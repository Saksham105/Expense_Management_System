import { useState, useEffect } from "react";

/* ══════════════════════════════════════════════════════════════════
   BACKEND API STUBS
   ➜ Replace each method body with your Java backend call.
   ➜ Keep the return shape identical so the UI works without changes.
══════════════════════════════════════════════════════════════════ */
const round2 = n => Math.round(((Number(n) || 0) + Number.EPSILON) * 100) / 100;
const makeAvatar = name => {
  const parts = String(name || "").trim().split(/\s+/).filter(Boolean);
  return ((parts[0]?.[0] || "") + (parts[1]?.[0] || parts[0]?.[1] || "")).toUpperCase() || "US";
};

const BackendService = (() => {
  const initialUsers = [
    { id:"u1", name:"saksham", email:"saksham@gmail.com", password:"pass1234", phone:"" },
    { id:"u2", name:"kirti", email:"k@gmail.com", password:"k1234", phone:"" },
    { id:"u3", name:"rupesh", email:"r@gmail.com", password:"r1234", phone:"" },
    { id:"u4", name:"amruta", email:"a@gmail.com", password:"a1234", phone:"" },
  ];

  const state = {
    users: initialUsers.map(user => ({ ...user, avatar: makeAvatar(user.name) })),
    groups: [],
    nextUserId: initialUsers.length + 1,
    nextGroupId: 1,
    nextExpenseId: 1,
  };

  const getUser = userId => state.users.find(user => user.id === String(userId));
  const getGroup = groupId => state.groups.find(group => group.id === String(groupId));
  const toUser = user => user && ({
    id: user.id,
    name: user.name,
    email: user.email,
    phone: user.phone || "",
    avatar: user.avatar || makeAvatar(user.name),
  });

  const computeBalances = group => {
    const balances = Object.fromEntries(group.memberIds.map(id => [id, 0]));

    for (const expense of group.expenses) {
      balances[expense.paidById] = round2((balances[expense.paidById] || 0) + expense.amount);
      for (const split of expense.splits) {
        balances[split.userId] = round2((balances[split.userId] || 0) - split.amount);
      }
    }

    for (const settlement of group.settlements) {
      balances[settlement.fromId] = round2((balances[settlement.fromId] || 0) + settlement.amount);
      balances[settlement.toId] = round2((balances[settlement.toId] || 0) - settlement.amount);
    }

    return balances;
  };

  const toUiExpense = expense => ({
    id: expense.id,
    title: expense.title,
    amount: expense.amount,
    paidBy: expense.paidByName,
    paidByName: expense.paidByName,
    strategy: expense.strategy,
    splits: expense.splits.map(split => ({ ...split })),
    date: expense.date,
  });

  const toUiGroup = (group, viewerId) => {
    const balances = computeBalances(group);
    return {
      id: group.id,
      name: group.name,
      members: group.memberIds.map(getUser).filter(Boolean).map(toUser),
      totalExpenses: round2(group.expenses.reduce((sum, expense) => sum + expense.amount, 0)),
      myBalance: round2(balances[String(viewerId)] || 0),
      expenses: group.expenses.map(toUiExpense),
    };
  };

  const normalizeSplits = (group, expense) => {
    if (expense.strategy === "ITEMWISE") {
      const totals = new Map();

      for (const item of expense.splits || []) {
        const assignedIds = (item.assignedTo?.length ? item.assignedTo : group.memberIds).map(String);
        const itemAmount = round2(parseFloat(item.itemAmount) || 0);
        if (!assignedIds.length || itemAmount <= 0) continue;

        const perHead = round2(itemAmount / assignedIds.length);
        for (const userId of assignedIds) {
          totals.set(userId, round2((totals.get(userId) || 0) + perHead));
        }
      }

      return [...totals.entries()].map(([userId, amount]) => ({
        userId,
        name: getUser(userId)?.name || userId,
        amount,
      }));
    }

    return (expense.splits || []).map(split => ({
      userId: String(split.userId),
      name: split.name || getUser(split.userId)?.name || String(split.userId),
      amount: round2(parseFloat(split.amount) || 0),
      ...(split.pct !== undefined ? { pct: round2(parseFloat(split.pct) || 0) } : {}),
    }));
  };

  return {
    login(email, password) {
      const user = state.users.find(current =>
        current.email?.toLowerCase() === String(email || "").trim().toLowerCase() &&
        current.password === password
      );
      return user ? { ok: true, user: toUser(user) } : { ok: false };
    },

    register(name, email, password) {
      const trimmedName = String(name || "").trim();
      const trimmedEmail = String(email || "").trim().toLowerCase();
      if (!trimmedName || !trimmedEmail || !password) return { ok: false };
      if (state.users.some(user => user.email?.toLowerCase() === trimmedEmail)) return { ok: false };

      const user = {
        id: `u${state.nextUserId++}`,
        name: trimmedName,
        email: trimmedEmail,
        password,
        phone: "",
        avatar: makeAvatar(trimmedName),
      };
      state.users.push(user);
      return { ok: true, user: toUser(user) };
    },

    updateProfile(userId, data) {
      const user = getUser(userId);
      if (!user) return { ok: false };

      user.name = String(data?.name || user.name).trim() || user.name;
      user.phone = String(data?.phone || "").trim();
      user.avatar = makeAvatar(user.name);
      return { ok: true, user: toUser(user) };
    },

    getAllUsers() {
      return state.users.map(toUser);
    },

    getGroupsForUser(userId) {
      return state.groups
        .filter(group => group.memberIds.includes(String(userId)))
        .map(group => toUiGroup(group, userId));
    },

    createGroup(name, memberIds, creatorId) {
      const creator = getUser(creatorId);
      const uniqueMemberIds = [...new Set([String(creatorId), ...(memberIds || []).map(String)])]
        .filter(id => getUser(id));
      if (!creator || !String(name || "").trim()) return null;

      const group = {
        id: `g${state.nextGroupId++}`,
        name: String(name).trim(),
        memberIds: uniqueMemberIds,
        expenses: [],
        settlements: [],
      };

      state.groups.unshift(group);
      return toUiGroup(group, creatorId);
    },

    addMembersToGroup(groupId, memberIds) {
      const group = getGroup(groupId);
      if (!group) return false;

      for (const userId of [...new Set((memberIds || []).map(String))]) {
        if (getUser(userId) && !group.memberIds.includes(userId)) {
          group.memberIds.push(userId);
        }
      }

      return true;
    },

    createExpense(groupId, expense) {
      const group = getGroup(groupId);
      const payer = getUser(expense?.paidBy);
      if (!group || !payer) return null;

      const created = {
        id: `e${state.nextExpenseId++}`,
        title: String(expense.title || "").trim(),
        amount: round2(parseFloat(expense.amount) || 0),
        paidById: payer.id,
        paidByName: payer.name,
        strategy: expense.strategy,
        splits: normalizeSplits(group, expense),
        date: expense.date || new Date().toISOString().split("T")[0],
      };

      if (!created.title || created.amount <= 0 || !created.splits.length) return null;

      group.expenses.unshift(created);
      return created;
    },

    getBalances(groupId) {
      const group = getGroup(groupId);
      return group ? computeBalances(group) : {};
    },

    settleUp(groupId, fromUserId, toUserId, amount) {
      const group = getGroup(groupId);
      const settledAmount = round2(parseFloat(amount) || 0);
      if (!group || !getUser(fromUserId) || !getUser(toUserId) || settledAmount <= 0) return false;

      group.settlements.unshift({
        fromId: String(fromUserId),
        toId: String(toUserId),
        amount: settledAmount,
        date: new Date().toISOString().split("T")[0],
      });

      return true;
    },
  };
})();

const API = {
  login: async (email, pw) => BackendService.login(email, pw),
  register: async (name, email, pw) => BackendService.register(name, email, pw),
  updateProfile: async (uid, data) => BackendService.updateProfile(uid, data),
  getUsers: async () => BackendService.getAllUsers(),
  getUserGroups: async uid => BackendService.getGroupsForUser(uid),
  createGroup: async (name, memberIds, creatorId) => {
    const group = BackendService.createGroup(name, memberIds, creatorId);
    return group ? { ok: true, id: group.id, group } : { ok: false };
  },
  addMembers: async (gid, memberIds) => ({ ok: BackendService.addMembersToGroup(gid, memberIds) }),
  createExpense: async (gid, expense) => {
    const created = BackendService.createExpense(gid, expense);
    return created ? { ok: true, id: created.id } : { ok: false };
  },
  getBalances: async gid => BackendService.getBalances(gid),
  settleUp: async (gid, fromId, toId, amount) => ({ ok: BackendService.settleUp(gid, fromId, toId, amount) }),
};

/* ── Mock seed data (replace with real API responses) ── */
const MOCK_GROUPS = [
  { id:"g1", name:"Goa Trip 🏖️",
    members:[{id:"u1",name:"Alex Mercer",avatar:"AM"},{id:"u2",name:"Zara Khan",avatar:"ZK"},{id:"u3",name:"Ryuk Tanaka",avatar:"RT"}],
    totalExpenses:12400, myBalance:-850,
    expenses:[
      { id:"e1", title:"Hotel Booking",      amount:6000, paidBy:"Zara Khan",   strategy:"EQUAL",      date:"2025-03-15", splits:[{name:"Alex Mercer",amount:2000},{name:"Zara Khan",amount:2000},{name:"Ryuk Tanaka",amount:2000}] },
      { id:"e2", title:"Beach Shack Dinner", amount:2400, paidBy:"Alex Mercer", strategy:"PERCENTAGE",  date:"2025-03-16", splits:[{name:"Alex Mercer",amount:1200,pct:50},{name:"Zara Khan",amount:720,pct:30},{name:"Ryuk Tanaka",amount:480,pct:20}] },
    ]},
  { id:"g2", name:"Office Lunch 🍱",
    members:[{id:"u1",name:"Alex Mercer",avatar:"AM"},{id:"u4",name:"Nova Chen",avatar:"NC"},{id:"u5",name:"Vex Patel",avatar:"VP"}],
    totalExpenses:3200, myBalance:400,
    expenses:[
      { id:"e3", title:"Thursday Biryani", amount:960, paidBy:"Alex Mercer", strategy:"EQUAL", date:"2025-03-14", splits:[{name:"Alex Mercer",amount:320},{name:"Nova Chen",amount:320},{name:"Vex Patel",amount:320}] },
    ]},
  { id:"g3", name:"Flat Expenses 🏠",
    members:[{id:"u1",name:"Alex Mercer",avatar:"AM"},{id:"u6",name:"Cipher Jones",avatar:"CJ"}],
    totalExpenses:8700, myBalance:0, expenses:[] },
];

/* ════════════════════ GLOBAL STYLES ════════════════════ */
const Styles = () => (
  <style>{`
    @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Share+Tech+Mono&display=swap');
    *{box-sizing:border-box;margin:0;padding:0}
    body{font-family:'Share Tech Mono',monospace;color:#00ffff;background:#050508}
    ::-webkit-scrollbar{width:4px;height:4px}
    ::-webkit-scrollbar-track{background:#050508}
    ::-webkit-scrollbar-thumb{background:#00ffff;border-radius:0}

    @keyframes fadeUp{from{opacity:0;transform:translateY(14px)}to{opacity:1;transform:translateY(0)}}
    @keyframes scan{0%{top:-4%}100%{top:104%}}
    @keyframes borderPulse{0%,100%{border-color:rgba(0,255,255,.15)}50%{border-color:rgba(0,255,255,.4)}}
    .fadeUp{animation:fadeUp .35s ease forwards}

    /* inputs */
    .ci{background:rgba(0,255,255,.05);border:none;border-bottom:1px solid rgba(0,255,255,.4);
        color:#00ffff;padding:10px 4px;font-family:'Share Tech Mono',monospace;font-size:14px;
        width:100%;outline:none;transition:all .3s}
    .ci:focus{border-bottom-color:#00ffff;box-shadow:0 4px 14px rgba(0,255,255,.12);background:rgba(0,255,255,.07)}
    .ci::placeholder{color:rgba(0,255,255,.22)}
    .ci option{background:#050508;color:#00ffff}

    /* buttons */
    .btn{border:1px solid currentColor;background:transparent;color:#00ffff;padding:9px 20px;
         font-family:'Share Tech Mono',monospace;font-size:12px;cursor:pointer;
         text-transform:uppercase;letter-spacing:1.5px;transition:all .22s;
         clip-path:polygon(0 0,calc(100% - 8px) 0,100% 8px,100% 100%,8px 100%,0 calc(100% - 8px))}
    .btn:hover{background:rgba(0,255,255,.1);box-shadow:0 0 14px rgba(0,255,255,.35)}
    .btn:disabled{opacity:.4;cursor:not-allowed}
    .btn-p{background:rgba(0,255,255,.1);box-shadow:0 0 8px rgba(0,255,255,.25)}
    .btn-p:hover{background:rgba(0,255,255,.18);box-shadow:0 0 20px rgba(0,255,255,.5)}
    .btn-pk{color:#ff00ff;border-color:#ff00ff}
    .btn-pk:hover{background:rgba(255,0,255,.1);box-shadow:0 0 14px rgba(255,0,255,.4)}
    .btn-r{color:#ff0066;border-color:#ff0066}
    .btn-r:hover{background:rgba(255,0,102,.1);box-shadow:0 0 14px rgba(255,0,102,.4)}
    .btn-g{color:#00ff88;border-color:#00ff88}
    .btn-g:hover{background:rgba(0,255,136,.1);box-shadow:0 0 14px rgba(0,255,136,.4)}
    .btn-y{color:#ffff00;border-color:#ffff00}
    .btn-y:hover{background:rgba(255,255,0,.08);box-shadow:0 0 14px rgba(255,255,0,.35)}
    .btn-sm{padding:5px 12px;font-size:11px}

    /* cards */
    .card{background:#0a0a18;border:1px solid rgba(0,255,255,.15);position:relative}
    .card::before{content:'';position:absolute;top:0;left:0;width:12px;height:12px;border-top:2px solid #00ffff;border-left:2px solid #00ffff}
    .card::after{content:'';position:absolute;bottom:0;right:0;width:12px;height:12px;border-bottom:2px solid #00ffff;border-right:2px solid #00ffff}
    .card-hover{transition:all .25s;cursor:pointer}
    .card-hover:hover{border-color:rgba(0,255,255,.45);box-shadow:0 0 22px rgba(0,255,255,.1)}

    /* grid bg */
    .gbg{background-image:linear-gradient(rgba(0,255,255,.025) 1px,transparent 1px),
         linear-gradient(90deg,rgba(0,255,255,.025) 1px,transparent 1px);background-size:28px 28px}

    /* modal */
    .ov{position:fixed;inset:0;background:rgba(0,0,0,.88);z-index:900;
        display:flex;align-items:center;justify-content:center;backdrop-filter:blur(5px)}
    .modal{background:#050510;border:1px solid #00ffff;box-shadow:0 0 50px rgba(0,255,255,.18);
           width:90%;max-width:540px;max-height:88vh;overflow-y:auto;
           padding:28px;position:relative;animation:fadeUp .3s ease}

    /* tabs */
    .tab{background:transparent;border:none;border-bottom:2px solid transparent;
         color:rgba(0,255,255,.38);padding:8px 18px;cursor:pointer;
         font-family:'Share Tech Mono',monospace;font-size:12px;
         text-transform:uppercase;letter-spacing:1px;transition:all .2s}
    .tab.act{color:#00ffff;border-bottom-color:#00ffff;text-shadow:0 0 8px #00ffff}
    .tab:hover:not(.act){color:rgba(0,255,255,.65)}

    /* member chips */
    .chip{display:inline-flex;align-items:center;gap:6px;padding:5px 11px;
          border:1px solid rgba(255,0,255,.3);color:#ff00ff;font-size:12px;
          margin:3px;cursor:pointer;transition:all .2s;background:transparent}
    .chip:hover,.chip.on{background:rgba(255,0,255,.12);box-shadow:0 0 9px rgba(255,0,255,.3);border-color:#ff00ff}
    .chip.off{opacity:.38}

    /* text utils */
    .ob{font-family:'Orbitron',sans-serif}
    .pos{color:#00ff88;text-shadow:0 0 6px #00ff88}
    .neg{color:#ff0066;text-shadow:0 0 6px #ff0066}
    .zero{color:rgba(0,255,255,.3)}
    .lbl{font-size:10px;letter-spacing:2px;color:rgba(0,255,255,.4);text-transform:uppercase;margin-bottom:6px}

    /* section rule */
    .sr{font-size:10px;letter-spacing:3px;color:rgba(0,255,255,.4);text-transform:uppercase;
        margin-bottom:14px;display:flex;align-items:center;gap:10px}
    .sr::after{content:'';flex:1;height:1px;background:rgba(0,255,255,.12)}

    /* avatar */
    .av{display:flex;align-items:center;justify-content:center;font-family:'Orbitron',sans-serif;font-weight:700;flex-shrink:0}
  `}</style>
);

/* ════ SMALL HELPERS ════ */
const Av = ({ i, s=34, c="#00ffff" }) => (
  <div className="av" style={{ width:s, height:s, background:`rgba(0,255,255,.07)`, border:`1px solid ${c}`,
    color:c, fontSize:s*.3, boxShadow:`0 0 8px ${c}40` }}>{i}</div>
);

const Inp = ({ label, ...p }) => (
  <div style={{ marginBottom:18 }}>
    {label && <div className="lbl">{label}</div>}
    <input className="ci" {...p} />
  </div>
);

const Sel = ({ label, children, ...p }) => (
  <div style={{ marginBottom:18 }}>
    {label && <div className="lbl">{label}</div>}
    <select className="ci" {...p}>{children}</select>
  </div>
);

const Logo = ({ size=30 }) => (
  <svg width={size} height={size} viewBox="0 0 56 56">
    <polygon points="28,3 46,16 38,16 44,38 28,53 12,38 18,16 10,16"
      fill="none" stroke="#00ffff" strokeWidth="1.8"/>
    <polygon points="28,11 40,21 33,21 37,37 28,47 19,37 23,21 16,21"
      fill="rgba(0,255,255,.07)" stroke="#ff00ff" strokeWidth="1.1"/>
    <line x1="28" y1="11" x2="28" y2="47" stroke="#00ffff" strokeWidth="1.4" strokeDasharray="2,4"/>
    <circle cx="28" cy="29" r="3" fill="#ff00ff" style={{filter:'drop-shadow(0 0 4px #ff00ff)'}}/>
  </svg>
);

const Err = ({ msg }) => msg ? <div style={{ color:"#ff0066", fontSize:12, marginBottom:12, textShadow:"0 0 6px #ff0066" }}>⚠ {msg}</div> : null;

/* ════════════════════════════════════════════════════════
   SCREEN: LOGIN
════════════════════════════════════════════════════════ */
function LoginScreen({ onLogin, onReg }) {
  const [email, setEmail] = useState(""); const [pw, setPw] = useState("");
  const [err, setErr] = useState(""); const [busy, setBusy] = useState(false);

  const go = async () => {
    if (!email||!pw) { setErr("All fields are required."); return; }
    setBusy(true); setErr("");
    const r = await API.login(email, pw);
    setBusy(false);
    r.ok ? onLogin(r.user) : setErr("Invalid credentials. Try again.");
  };

  return (
    <div className="gbg" style={{ minHeight:"100vh", display:"flex", alignItems:"center", justifyContent:"center" }}>
      <div style={{ position:"fixed", inset:0, pointerEvents:"none", overflow:"hidden" }}>
        <div style={{ position:"absolute", left:0, right:0, height:2, background:"rgba(0,255,255,.04)", animation:"scan 6s linear infinite" }}/>
      </div>
      <div className="card fadeUp" style={{ width:"100%", maxWidth:420, padding:44, zIndex:1 }}>
        <div style={{ textAlign:"center", marginBottom:38 }}>
          <Logo size={60}/>
          <div className="ob" style={{ fontSize:28, fontWeight:900, letterSpacing:5, color:"#00ffff", textShadow:"0 0 22px #00ffff", marginTop:12 }}>SPLITR</div>
          <div style={{ fontSize:10, letterSpacing:4, color:"rgba(0,255,255,.3)", marginTop:5 }}>EXPENSE NETWORK v2.0</div>
        </div>
        <div className="sr">Auth Terminal</div>
        <Inp label="Email / User ID" type="email" placeholder="user@matrix.io" value={email} onChange={e=>setEmail(e.target.value)}/>
        <Inp label="Password" type="password" placeholder="••••••••" value={pw} onChange={e=>setPw(e.target.value)} onKeyDown={e=>e.key==="Enter"&&go()}/>
        <Err msg={err}/>
        <button className="btn btn-p" style={{ width:"100%", marginBottom:16, padding:"12px 20px" }} onClick={go} disabled={busy}>
          {busy ? "Authenticating…" : "▶  Login"}
        </button>
        <div style={{ textAlign:"center", fontSize:12, color:"rgba(0,255,255,.3)" }}>
          No account?{" "}
          <span style={{ color:"#ff00ff", cursor:"pointer", textShadow:"0 0 6px #ff00ff" }} onClick={onReg}>Create Identity →</span>
        </div>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════
   SCREEN: REGISTER
════════════════════════════════════════════════════════ */
function RegisterScreen({ onDone, onLogin }) {
  const [f, setF] = useState({ name:"", email:"", pw:"", pw2:"" });
  const [err, setErr] = useState(""); const [busy, setBusy] = useState(false);
  const s = k => e => setF(x=>({...x,[k]:e.target.value}));

  const go = async () => {
    if (!f.name||!f.email||!f.pw) { setErr("All fields required."); return; }
    if (f.pw !== f.pw2) { setErr("Passwords do not match."); return; }
    setBusy(true); setErr("");
    const r = await API.register(f.name, f.email, f.pw);
    setBusy(false);
    r.ok ? onDone() : setErr("Registration failed. Try again.");
  };

  return (
    <div className="gbg" style={{ minHeight:"100vh", display:"flex", alignItems:"center", justifyContent:"center" }}>
      <div className="card fadeUp" style={{ width:"100%", maxWidth:420, padding:44 }}>
        <div style={{ textAlign:"center", marginBottom:34 }}>
          <Logo size={48}/>
          <div className="ob" style={{ fontSize:22, fontWeight:900, letterSpacing:4, color:"#ff00ff", textShadow:"0 0 16px #ff00ff", marginTop:12 }}>SPLITR</div>
          <div style={{ fontSize:10, letterSpacing:3, color:"rgba(255,0,255,.35)", marginTop:4 }}>Create New Identity</div>
        </div>
        <div className="sr">Registration</div>
        <Inp label="Display Name" placeholder="Your alias" value={f.name} onChange={s("name")}/>
        <Inp label="Email" type="email" placeholder="user@matrix.io" value={f.email} onChange={s("email")}/>
        <Inp label="Password" type="password" placeholder="Min 8 characters" value={f.pw} onChange={s("pw")}/>
        <Inp label="Confirm Password" type="password" placeholder="Repeat password" value={f.pw2} onChange={s("pw2")}/>
        <Err msg={err}/>
        <button className="btn btn-p" style={{ width:"100%", marginBottom:16 }} onClick={go} disabled={busy}>
          {busy ? "Creating…" : "▶  Create Account"}
        </button>
        <div style={{ textAlign:"center", fontSize:12, color:"rgba(0,255,255,.3)" }}>
          Already registered?{" "}
          <span style={{ color:"#00ffff", cursor:"pointer" }} onClick={onLogin}>Login →</span>
        </div>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════
   MODAL: CREATE GROUP
════════════════════════════════════════════════════════ */
function CreateGroupModal({ me, allUsers, onClose, onCreate }) {
  const [name, setName] = useState(""); const [sel, setSel] = useState([]);
  const [busy, setBusy] = useState(false);
  const toggle = u => setSel(s=>s.find(x=>x.id===u.id)?s.filter(x=>x.id!==u.id):[...s,u]);

  const go = async () => {
    if (!name.trim()) return;
    setBusy(true);
    const members = [me, ...sel];
    const r = await API.createGroup(name, members.map(m=>m.id), me.id);
    setBusy(false);
    if (r.ok && r.group) onCreate(r.group);
  };

  return (
    <div className="ov" onClick={e=>e.target===e.currentTarget&&onClose()}>
      <div className="modal">
        <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:24 }}>
          <div className="ob" style={{ fontSize:15, fontWeight:700, color:"#00ffff" }}>⬡ New Group</div>
          <button className="btn btn-sm" onClick={onClose}>✕</button>
        </div>
        <div className="sr">Group Name</div>
        <Inp placeholder="e.g. Goa Trip, Flat Mates…" value={name} onChange={e=>setName(e.target.value)}/>
        <div className="sr" style={{ marginTop:6 }}>Select Members</div>
        <div style={{ fontSize:11, color:"rgba(0,255,255,.35)", marginBottom:12 }}>You are added automatically.</div>
        <div style={{ marginBottom:18 }}>
          {allUsers.filter(u=>u.id!==me.id).map(u=>{
            const on = !!sel.find(x=>x.id===u.id);
            return <div key={u.id} className={`chip ${on?"on":"off"}`} onClick={()=>toggle(u)}>
              <Av i={u.avatar} s={20} c={on?"#ff00ff":"rgba(255,0,255,.4)"}/>
              {u.name}{on&&" ✓"}
            </div>;
          })}
        </div>
        <div style={{ fontSize:12, color:"rgba(0,255,255,.4)", marginBottom:20 }}>
          Total members: <span style={{ color:"#00ffff" }}>{1+sel.length}</span>
        </div>
        <button className="btn btn-p" style={{ width:"100%" }} onClick={go} disabled={busy||!name.trim()}>
          {busy?"Creating…":"▶  Create Group"}
        </button>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════
   MODAL: ADD MEMBERS
════════════════════════════════════════════════════════ */
function AddMembersModal({ group, allUsers, onClose, onAdd }) {
  const existIds = group.members.map(m=>m.id);
  const available = allUsers.filter(u=>!existIds.includes(u.id));
  const [sel, setSel] = useState([]); const [busy, setBusy] = useState(false);
  const toggle = u => setSel(s=>s.find(x=>x.id===u.id)?s.filter(x=>x.id!==u.id):[...s,u]);

  const go = async () => {
    if (!sel.length) return;
    setBusy(true);
    const r = await API.addMembers(group.id, sel.map(u=>u.id));
    setBusy(false);
    if (r.ok) onAdd(sel);
  };

  return (
    <div className="ov" onClick={e=>e.target===e.currentTarget&&onClose()}>
      <div className="modal">
        <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:22 }}>
          <div className="ob" style={{ fontSize:15, fontWeight:700, color:"#ff00ff" }}>⊕ Add Members</div>
          <button className="btn btn-sm" onClick={onClose}>✕</button>
        </div>
        <div style={{ fontSize:13, color:"rgba(0,255,255,.45)", marginBottom:16 }}>
          Group: <span style={{ color:"#00ffff" }}>{group.name}</span>
        </div>
        <div className="sr">Available Users</div>
        {available.length===0
          ? <div style={{ color:"rgba(0,255,255,.3)", fontSize:13 }}>All users are already in this group.</div>
          : available.map(u=>{
            const on = !!sel.find(x=>x.id===u.id);
            return <div key={u.id} className={`chip ${on?"on":"off"}`} onClick={()=>toggle(u)}>
              <Av i={u.avatar} s={20} c={on?"#ff00ff":"rgba(255,0,255,.4)"}/>
              {u.name}{on&&" ✓"}
            </div>;
          })}
        <button className="btn btn-pk" style={{ width:"100%", marginTop:24 }} onClick={go} disabled={busy||!sel.length}>
          {busy?"Adding…":`▶  Add ${sel.length||""} Member${sel.length!==1?"s":""}`}
        </button>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════
   MODAL: CREATE EXPENSE
════════════════════════════════════════════════════════ */
function CreateExpenseModal({ group, me, onClose, onCreate }) {
  const [strategy, setStrategy] = useState("EQUAL");
  const [title, setTitle] = useState(""); const [amt, setAmt] = useState("");
  const [paidBy, setPaidBy] = useState(me.id);
  const [pcts, setPcts] = useState({});
  const [items, setItems] = useState([{ name:"", amount:"", assignedTo:[] }]);
  const [busy, setBusy] = useState(false);

  const members = group.members;
  const totalPct = Object.values(pcts).reduce((a,b)=>a+(parseFloat(b)||0), 0);
  const pctOk = Math.abs(totalPct-100)<0.01;

  const addItem = () => setItems(it=>[...it,{ name:"", amount:"", assignedTo:[] }]);
  const editItem = (i,k,v) => setItems(it=>it.map((x,j)=>j===i?{...x,[k]:v}:x));
  const toggleIM = (i, mid) => setItems(it=>it.map((x,j)=>j===i?{...x, assignedTo:x.assignedTo.includes(mid)?x.assignedTo.filter(id=>id!==mid):[...x.assignedTo,mid]}:x));
  const rmItem = i => setItems(it=>it.filter((_,j)=>j!==i));

  const buildSplits = () => {
    const a = parseFloat(amt);
    if (strategy==="EQUAL") return members.map(m=>({ userId:m.id, name:m.name, amount:parseFloat((a/members.length).toFixed(2)) }));
    if (strategy==="PERCENTAGE") return members.map(m=>({ userId:m.id, name:m.name, pct:parseFloat(pcts[m.id]||0), amount:parseFloat((a*(parseFloat(pcts[m.id]||0)/100)).toFixed(2)) }));
    return items.map(it=>({ itemName:it.name, itemAmount:parseFloat(it.amount)||0, assignedTo:it.assignedTo }));
  };

  const canSubmit = title && amt && (strategy!=="PERCENTAGE" || pctOk);

  const go = async () => {
    if (!canSubmit) return;
    setBusy(true);
    const expense = { title, amount:parseFloat(amt), paidBy, paidByName:members.find(m=>m.id===paidBy)?.name, strategy, splits:buildSplits(), date:new Date().toISOString().split("T")[0] };
    const r = await API.createExpense(group.id, expense);
    setBusy(false);
    if (r.ok) onCreate({ ...expense, id:r.id });
  };

  const sCol = { EQUAL:"#00ffff", PERCENTAGE:"#ff00ff", ITEMWISE:"#ffff00" };

  return (
    <div className="ov" onClick={e=>e.target===e.currentTarget&&onClose()}>
      <div className="modal" style={{ maxWidth:580 }}>
        <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:22 }}>
          <div className="ob" style={{ fontSize:15, fontWeight:700, color:"#00ffff" }}>+ New Expense</div>
          <button className="btn btn-sm" onClick={onClose}>✕</button>
        </div>

        <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:14 }}>
          <Inp label="Title" placeholder="e.g. Hotel Booking" value={title} onChange={e=>setTitle(e.target.value)}/>
          <Inp label="Amount (₹)" type="number" placeholder="0.00" value={amt} onChange={e=>setAmt(e.target.value)}/>
        </div>
        <Sel label="Paid By" value={paidBy} onChange={e=>setPaidBy(e.target.value)}>
          {members.map(m=><option key={m.id} value={m.id}>{m.name}</option>)}
        </Sel>

        <div className="sr" style={{ marginTop:4 }}>Split Strategy</div>
        <div style={{ display:"flex", borderBottom:"1px solid rgba(0,255,255,.12)", marginBottom:20 }}>
          {["EQUAL","PERCENTAGE","ITEMWISE"].map(t=>(
            <button key={t} className={`tab ${strategy===t?"act":""}`} onClick={()=>setStrategy(t)}
              style={strategy===t?{borderBottomColor:sCol[t],color:sCol[t],textShadow:`0 0 8px ${sCol[t]}`}:{}}>{t}</button>
          ))}
        </div>

        {strategy==="EQUAL" && (
          <div style={{ fontSize:13, color:"rgba(0,255,255,.55)", padding:"12px 16px", background:"rgba(0,255,255,.04)", border:"1px solid rgba(0,255,255,.1)" }}>
            Split equally among <span style={{ color:"#00ffff" }}>{members.length}</span> members.
            {amt && <div style={{ marginTop:8, color:"#00ffff" }}>Each pays: <span className="ob" style={{ color:"#ffff00", textShadow:"0 0 8px #ffff00" }}>₹{(parseFloat(amt)/members.length).toFixed(2)}</span></div>}
          </div>
        )}

        {strategy==="PERCENTAGE" && (
          <div>
            {members.map(m=>(
              <div key={m.id} style={{ display:"flex", alignItems:"center", gap:12, marginBottom:12 }}>
                <Av i={m.avatar||m.name.slice(0,2).toUpperCase()} s={30}/>
                <span style={{ flex:1, fontSize:13 }}>{m.name}</span>
                <input className="ci" type="number" min="0" max="100" placeholder="0"
                  style={{ width:72 }} value={pcts[m.id]||""} onChange={e=>setPcts(p=>({...p,[m.id]:e.target.value}))}/>
                <span style={{ fontSize:12, color:"rgba(0,255,255,.4)" }}>%</span>
                {amt&&pcts[m.id]&&<span style={{ fontSize:11, color:"#ffff00", width:60, textAlign:"right" }}>₹{(parseFloat(amt)*(parseFloat(pcts[m.id]||0)/100)).toFixed(2)}</span>}
              </div>
            ))}
            <div style={{ fontSize:12, marginTop:8, color:pctOk?"#00ff88":"#ff0066", textShadow:`0 0 6px ${pctOk?"#00ff88":"#ff0066"}` }}>
              Total: {totalPct.toFixed(1)}% {pctOk?"✓ Valid":"← must equal 100%"}
            </div>
          </div>
        )}

        {strategy==="ITEMWISE" && (
          <div>
            {items.map((it,i)=>(
              <div key={i} style={{ background:"rgba(0,255,255,.03)", border:"1px solid rgba(0,255,255,.1)", padding:14, marginBottom:12 }}>
                <div style={{ display:"flex", gap:10, marginBottom:10 }}>
                  <input className="ci" placeholder="Item name" value={it.name} onChange={e=>editItem(i,"name",e.target.value)} style={{ flex:1 }}/>
                  <input className="ci" type="number" placeholder="₹" style={{ width:90 }} value={it.amount} onChange={e=>editItem(i,"amount",e.target.value)}/>
                  {items.length>1&&<button className="btn btn-r btn-sm" onClick={()=>rmItem(i)}>✕</button>}
                </div>
                <div style={{ fontSize:10, letterSpacing:2, color:"rgba(0,255,255,.4)", marginBottom:7 }}>ASSIGN TO:</div>
                {members.map(m=>{
                  const on = it.assignedTo.includes(m.id);
                  return <div key={m.id} className={`chip ${on?"on":"off"}`} style={{ fontSize:11 }} onClick={()=>toggleIM(i,m.id)}>
                    {m.name.split(" ")[0]}{on&&" ✓"}
                  </div>;
                })}
              </div>
            ))}
            <button className="btn btn-sm" style={{ marginTop:4 }} onClick={addItem}>+ Add Item</button>
          </div>
        )}

        <button className="btn btn-p" style={{ width:"100%", marginTop:24 }} onClick={go} disabled={busy||!canSubmit}>
          {busy?"Saving…":"▶  Add Expense"}
        </button>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════
   MODAL: EDIT PROFILE
════════════════════════════════════════════════════════ */
function EditProfileModal({ user, onClose, onSave }) {
  const [f, setF] = useState({ name:user.name, phone:user.phone||"" });
  const [busy, setBusy] = useState(false);
  const s = k => e => setF(x=>({...x,[k]:e.target.value}));

  const go = async () => {
    setBusy(true);
    await API.updateProfile(user.id, f);
    setBusy(false);
    onSave({ ...user, ...f });
  };

  return (
    <div className="ov" onClick={e=>e.target===e.currentTarget&&onClose()}>
      <div className="modal" style={{ maxWidth:400 }}>
        <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:24 }}>
          <div className="ob" style={{ fontSize:15, fontWeight:700, color:"#ff00ff" }}>✎ Edit Profile</div>
          <button className="btn btn-sm" onClick={onClose}>✕</button>
        </div>
        <div style={{ textAlign:"center", marginBottom:24 }}>
          <Av i={user.avatar} s={72} c="#ff00ff"/>
        </div>
        <Inp label="Display Name" value={f.name} onChange={s("name")}/>
        <div style={{ marginBottom:18 }}>
          <div className="lbl">Email (read-only)</div>
          <div style={{ fontSize:13, color:"rgba(0,255,255,.5)", padding:"10px 4px", borderBottom:"1px solid rgba(0,255,255,.1)" }}>{user.email}</div>
        </div>
        <Inp label="Phone" placeholder="+91 00000 00000" value={f.phone} onChange={s("phone")}/>
        <button className="btn btn-pk" style={{ width:"100%" }} onClick={go} disabled={busy}>
          {busy?"Saving…":"▶  Save Changes"}
        </button>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════
   MODAL: SETTLE UP
════════════════════════════════════════════════════════ */
function SettleUpModal({ group, me, onClose, onSettle }) {
  const [from, setFrom] = useState(me.id);
  const [to, setTo] = useState(""); const [amt, setAmt] = useState("");
  const [busy, setBusy] = useState(false);

  const go = async () => {
    if (!to||!amt) return;
    setBusy(true);
    await API.settleUp(group.id, from, to, parseFloat(amt));
    setBusy(false);
    onSettle({ from, to, amount:parseFloat(amt) });
  };

  return (
    <div className="ov" onClick={e=>e.target===e.currentTarget&&onClose()}>
      <div className="modal" style={{ maxWidth:400 }}>
        <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:24 }}>
          <div className="ob" style={{ fontSize:15, fontWeight:700, color:"#00ff88" }}>⚡ Settle Up</div>
          <button className="btn btn-sm" onClick={onClose}>✕</button>
        </div>
        <Sel label="Payment From" value={from} onChange={e=>{setFrom(e.target.value);setTo("")}}>
          {group.members.map(m=><option key={m.id} value={m.id}>{m.name}</option>)}
        </Sel>
        <Sel label="Payment To" value={to} onChange={e=>setTo(e.target.value)}>
          <option value="">— Select recipient —</option>
          {group.members.filter(m=>m.id!==from).map(m=><option key={m.id} value={m.id}>{m.name}</option>)}
        </Sel>
        <Inp label="Amount (₹)" type="number" placeholder="0.00" value={amt} onChange={e=>setAmt(e.target.value)}/>
        <button className="btn btn-g" style={{ width:"100%" }} onClick={go} disabled={busy||!to||!amt}>
          {busy?"Processing…":"⚡  Confirm Settlement"}
        </button>
      </div>
    </div>
  );
}

/* ════════════════════════════════════════════════════════
   SCREEN: GROUP VIEW
════════════════════════════════════════════════════════ */
function GroupView({ group, me, allUsers, onBack, onUpdate }) {
  const [grp, setGrp] = useState(group);
  const [balances, setBalances] = useState({});
  const [modal, setModal] = useState(null);
  const [activeTab, setActiveTab] = useState("expenses");

  useEffect(() => { setGrp(group); }, [group]);

  const mutate = (updated) => { setGrp(updated); onUpdate(updated); };

  const refreshGroup = async () => {
    const [groups, nextBalances] = await Promise.all([
      API.getUserGroups(me.id),
      API.getBalances(grp.id),
    ]);
    const fresh = groups.find(g => g.id === grp.id);
    if (fresh) mutate(fresh);
    setBalances(nextBalances);
  };

  useEffect(() => {
    let active = true;
    API.getBalances(grp.id).then(nextBalances => { if (active) setBalances(nextBalances); });
    return () => { active = false; };
  }, [grp.id, grp.expenses.length, grp.members.length]);

  const addExpense = async () => { await refreshGroup(); setModal(null); };
  const addMembers = async () => { await refreshGroup(); setModal(null); };
  const handleSettle = async () => {
    await refreshGroup();
    setModal(null);
  };

  const sCol = s=>({ EQUAL:"#00ffff", PERCENTAGE:"#ff00ff", ITEMWISE:"#ffff00" }[s]||"#00ffff");
  const myBalance = round2(balances[me.id] ?? grp.myBalance ?? 0);

  return (
    <div style={{ minHeight:"100vh", background:"#050508" }} className="gbg">
      {/* ─── Top bar ─── */}
      <div style={{ background:"rgba(8,8,20,.97)", borderBottom:"1px solid rgba(0,255,255,.15)", padding:"12px 22px",
        display:"flex", alignItems:"center", gap:14, position:"sticky", top:0, zIndex:50, backdropFilter:"blur(8px)" }}>
        <button className="btn btn-sm" onClick={onBack}>◁ Back</button>
        <Logo size={22}/>
        <div className="ob" style={{ fontSize:16, fontWeight:700 }}>{grp.name}</div>
        <div style={{ marginLeft:"auto", display:"flex", gap:10 }}>
          <button className="btn btn-sm btn-pk" onClick={()=>setModal("addmembers")}>⊕ Members</button>
          <button className="btn btn-sm btn-g" onClick={()=>setModal("settleup")}>⚡ Settle Up</button>
          <button className="btn btn-sm btn-p" onClick={()=>setModal("expense")}>+ Expense</button>
        </div>
      </div>

      <div style={{ padding:22, maxWidth:860, margin:"0 auto" }}>
        {/* Members strip */}
        <div className="card" style={{ padding:"14px 18px", marginBottom:18, display:"flex", alignItems:"center", gap:12, flexWrap:"wrap" }}>
          <span style={{ fontSize:10, letterSpacing:2, color:"rgba(0,255,255,.4)", textTransform:"uppercase" }}>Members ({grp.members.length})</span>
          {grp.members.map(m=>(
            <div key={m.id} style={{ display:"flex", alignItems:"center", gap:6, fontSize:12 }}>
              <Av i={m.avatar||m.name.slice(0,2).toUpperCase()} s={26} c={m.id===me.id?"#ff00ff":"#00ffff"}/>
              <span style={{ color:m.id===me.id?"#ff00ff":"rgba(0,255,255,.8)" }}>{m.name}{m.id===me.id&&" (you)"}</span>
            </div>
          ))}
        </div>

        {/* Stats row */}
        <div style={{ display:"grid", gridTemplateColumns:"repeat(3,1fr)", gap:16, marginBottom:22 }}>
          {[
            { label:"Total Spent",    val:`₹${grp.totalExpenses.toLocaleString()}`, c:"#00ffff" },
            { label:"Your Balance",   val:myBalance===0?"Settled":myBalance>0?`+Rs.${myBalance}`:`-Rs.${Math.abs(myBalance)}`,
              c:myBalance>0?"#00ff88":myBalance<0?"#ff0066":"rgba(0,255,255,.3)" },
            { label:"Expenses",       val:grp.expenses.length, c:"#ff00ff" },
          ].map(s=>(
            <div key={s.label} className="card" style={{ padding:"16px 20px" }}>
              <div className="lbl" style={{ marginBottom:8 }}>{s.label}</div>
              <div className="ob" style={{ fontSize:20, fontWeight:700, color:s.c, textShadow:`0 0 10px ${s.c}` }}>{s.val}</div>
            </div>
          ))}
        </div>

        {/* Tabs */}
        <div style={{ borderBottom:"1px solid rgba(0,255,255,.12)", marginBottom:20 }}>
          {["expenses","balances"].map(t=>(
            <button key={t} className={`tab ${activeTab===t?"act":""}`} onClick={()=>setActiveTab(t)}>{t}</button>
          ))}
        </div>

        {/* ─── Expenses Tab ─── */}
        {activeTab==="expenses" && (
          <div className="fadeUp">
            {grp.expenses.length===0
              ? <div style={{ textAlign:"center", padding:60, color:"rgba(0,255,255,.22)", fontSize:14 }}>
                  <div style={{ fontSize:40, marginBottom:12 }}>₿</div>
                  No expenses yet. Hit <span style={{ color:"#00ffff" }}>+ Expense</span> to start.
                </div>
              : grp.expenses.map(exp=>(
                <div key={exp.id} style={{ borderLeft:`3px solid ${sCol(exp.strategy)}`, padding:"14px 18px",
                  marginBottom:12, background:"rgba(0,255,255,.03)", transition:"background .2s" }}
                  onMouseEnter={e=>e.currentTarget.style.background="rgba(0,255,255,.07)"}
                  onMouseLeave={e=>e.currentTarget.style.background="rgba(0,255,255,.03)"}>
                  <div style={{ display:"flex", justifyContent:"space-between", alignItems:"flex-start", marginBottom:10 }}>
                    <div>
                      <div style={{ fontSize:15, fontWeight:700, marginBottom:4 }}>{exp.title}</div>
                      <div style={{ fontSize:11, color:"rgba(0,255,255,.38)" }}>
                        Paid by <span style={{ color:"#00ffff" }}>{exp.paidBy||exp.paidByName}</span> · {exp.date}
                      </div>
                    </div>
                    <div style={{ textAlign:"right" }}>
                      <div className="ob" style={{ fontSize:18, fontWeight:700, color:"#ffff00", textShadow:"0 0 10px #ffff00" }}>
                        ₹{(exp.amount||0).toLocaleString()}
                      </div>
                      <div style={{ fontSize:10, padding:"2px 8px", border:`1px solid ${sCol(exp.strategy)}`, color:sCol(exp.strategy), marginTop:4, display:"inline-block" }}>
                        {exp.strategy}
                      </div>
                    </div>
                  </div>
                  {exp.splits && exp.splits.length>0 && (
                    <div style={{ display:"flex", flexWrap:"wrap", gap:6 }}>
                      {exp.splits.map((sp,i)=>(
                        <div key={i} style={{ fontSize:11, padding:"2px 9px", border:"1px solid rgba(0,255,255,.15)", color:"rgba(0,255,255,.6)" }}>
                          {sp.name}: ₹{sp.amount}{sp.pct!==undefined&&` (${sp.pct}%)`}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))
            }
          </div>
        )}

        {/* ─── Balances Tab ─── */}
        {activeTab==="balances" && (
          <div className="fadeUp">
            <div style={{ fontSize:11, color:"rgba(0,255,255,.3)", marginBottom:16, padding:"8px 14px", border:"1px solid rgba(0,255,255,.1)", background:"rgba(0,255,255,.02)" }}>
              ℹ Balances are computed by the backend.{" "}
              Below shows the balance field from group data.
            </div>
            {grp.members.map(m=>{
              const memberBalance = round2(balances[m.id] ?? 0);
              return (
                <div key={m.id} style={{ display:"flex", alignItems:"center", gap:16, padding:"14px 18px",
                  marginBottom:10, background:"rgba(0,255,255,.03)", border:"1px solid rgba(0,255,255,.08)" }}>
                  <Av i={m.avatar||m.name.slice(0,2).toUpperCase()} s={38} c={m.id===me.id?"#ff00ff":"#00ffff"}/>
                  <div style={{ flex:1 }}>
                    <div style={{ fontSize:14 }}>{m.name}{m.id===me.id&&<span style={{ fontSize:10, color:"rgba(255,0,255,.5)", marginLeft:6 }}>(you)</span>}</div>
                    <div style={{ fontSize:10, color:"rgba(0,255,255,.3)", marginTop:2 }}>{m.id}</div>
                  </div>
                  <div className="ob" style={{ fontSize:16, fontWeight:700 }}
                    className={memberBalance>0?"pos":memberBalance<0?"neg":"zero"}>
                    {memberBalance===0?"Settled":memberBalance>0?`+Rs.${memberBalance}`:`-Rs.${Math.abs(memberBalance)}`}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {modal==="expense"    && <CreateExpenseModal group={grp} me={me} onClose={()=>setModal(null)} onCreate={addExpense}/>}
      {modal==="addmembers" && <AddMembersModal group={grp} allUsers={allUsers} onClose={()=>setModal(null)} onAdd={addMembers}/>}
      {modal==="settleup"   && <SettleUpModal group={grp} me={me} onClose={()=>setModal(null)} onSettle={handleSettle}/>}
    </div>
  );
}

/* ════════════════════════════════════════════════════════
   SCREEN: DASHBOARD
════════════════════════════════════════════════════════ */
function Dashboard({ user, allUsers, onLogout, onOpenGroup }) {
  const [me, setMe] = useState(user);
  const [groups, setGroups] = useState([]);
  const [modal, setModal] = useState(null);
  const [sideOpen, setSideOpen] = useState(false);

  useEffect(() => {
    API.getUserGroups(me.id).then(setGroups);
  }, [me.id]);

  const totalOwed  = groups.reduce((a,g)=>a+(g.myBalance<0?Math.abs(g.myBalance):0),0);
  const totalOwing = groups.reduce((a,g)=>a+(g.myBalance>0?g.myBalance:0),0);

  const updateGroup = g => setGroups(gs=>gs.map(x=>x.id===g.id?g:x));

  return (
    <div style={{ minHeight:"100vh", background:"#050508", display:"flex", flexDirection:"column" }} className="gbg">
      {/* ─── Topbar ─── */}
      <div style={{ background:"rgba(8,8,20,.97)", borderBottom:"1px solid rgba(0,255,255,.15)", padding:"12px 22px",
        display:"flex", alignItems:"center", gap:14, position:"sticky", top:0, zIndex:50, backdropFilter:"blur(8px)" }}>
        <div style={{ display:"flex", alignItems:"center", gap:10 }}>
          <Logo size={28}/>
          <span className="ob" style={{ fontSize:17, fontWeight:900, letterSpacing:4, color:"#00ffff", textShadow:"0 0 12px #00ffff" }}>SPLITR</span>
        </div>
        <div style={{ flex:1 }}/>
        <div style={{ display:"flex", alignItems:"center", gap:10, cursor:"pointer", padding:"4px 10px",
          border:"1px solid rgba(255,0,255,.2)", transition:"all .2s" }}
          onClick={()=>setSideOpen(p=>!p)}
          onMouseEnter={e=>e.currentTarget.style.borderColor="rgba(255,0,255,.5)"}
          onMouseLeave={e=>e.currentTarget.style.borderColor="rgba(255,0,255,.2)"}>
          <Av i={me.avatar} s={30} c="#ff00ff"/>
          <span style={{ fontSize:13, color:"rgba(0,255,255,.7)" }}>{me.name}</span>
          <span style={{ fontSize:10, color:"rgba(255,0,255,.5)" }}>{sideOpen?"▲":"▼"}</span>
        </div>
        <button className="btn btn-r btn-sm" onClick={onLogout}>⏻ Logout</button>
      </div>

      <div style={{ display:"flex", flex:1, overflow:"hidden" }}>
        {/* ─── Profile Sidebar ─── */}
        <div style={{ width:sideOpen?276:0, flexShrink:0, overflow:"hidden", transition:"width .32s ease" }}>
          <div style={{ width:276, background:"rgba(8,8,22,.98)", borderRight:"1px solid rgba(0,255,255,.13)",
            minHeight:"100%", padding:24, overflowY:"auto" }}>
            <div className="sr">Identity</div>
            <div style={{ textAlign:"center", marginBottom:22 }}>
              <Av i={me.avatar} s={68} c="#ff00ff"/>
              <div className="ob" style={{ fontSize:14, fontWeight:700, marginTop:12 }}>{me.name}</div>
              <div style={{ fontSize:11, color:"rgba(0,255,255,.38)", marginTop:4 }}>{me.email}</div>
              {me.phone&&<div style={{ fontSize:11, color:"rgba(0,255,255,.3)", marginTop:3 }}>{me.phone}</div>}
            </div>
            <button className="btn btn-pk" style={{ width:"100%", marginBottom:22 }} onClick={()=>setModal("editProfile")}>✎ Edit Profile</button>

            <div className="sr">Financial Summary</div>
            <div style={{ marginBottom:16 }}>
              <div className="lbl">You Owe</div>
              <div className="ob" style={{ fontSize:20, color:"#ff0066", textShadow:"0 0 10px #ff0066" }}>₹{totalOwed.toLocaleString()}</div>
            </div>
            <div style={{ marginBottom:22 }}>
              <div className="lbl">Owed to You</div>
              <div className="ob" style={{ fontSize:20, color:"#00ff88", textShadow:"0 0 10px #00ff88" }}>₹{totalOwing.toLocaleString()}</div>
            </div>

            <div className="sr">Groups</div>
            <div className="ob" style={{ fontSize:32, fontWeight:700, color:"#00ffff" }}>{groups.length}</div>
            <div style={{ fontSize:11, color:"rgba(0,255,255,.35)", marginTop:4 }}>Active groups</div>
          </div>
        </div>

        {/* ─── Main Content ─── */}
        <div style={{ flex:1, padding:22, overflowY:"auto" }}>
          <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:24 }}>
            <div>
              <div className="ob" style={{ fontSize:20, fontWeight:700 }}>My Groups</div>
              <div style={{ fontSize:11, color:"rgba(0,255,255,.3)", marginTop:5 }}>
                {groups.length} active group{groups.length!==1?"s":""} · Select to manage
              </div>
            </div>
            <button className="btn btn-p" onClick={()=>setModal("createGroup")}>⊕ New Group</button>
          </div>

          {groups.length===0
            ? <div style={{ textAlign:"center", padding:80, color:"rgba(0,255,255,.2)" }}>
                <div style={{ fontSize:56, marginBottom:16 }}>⬡</div>
                <div>No groups yet.</div>
                <div style={{ marginTop:8, fontSize:12 }}>Create your first group to start splitting.</div>
              </div>
            : <div style={{ display:"grid", gridTemplateColumns:"repeat(auto-fill,minmax(280px,1fr))", gap:18 }}>
                {groups.map(g=>(
                  <div key={g.id} className="card card-hover" style={{ padding:20 }} onClick={()=>onOpenGroup(g, updateGroup)}>
                    <div className="ob" style={{ fontSize:14, fontWeight:700, marginBottom:14 }}>{g.name}</div>
                    <div style={{ display:"flex", gap:4, marginBottom:16, alignItems:"center" }}>
                      {g.members.slice(0,5).map(m=>(
                        <Av key={m.id} i={m.avatar||m.name.slice(0,2).toUpperCase()} s={26} c={m.id===me.id?"#ff00ff":"#00ffff"}/>
                      ))}
                      {g.members.length>5&&<span style={{ fontSize:11, color:"rgba(0,255,255,.38)", marginLeft:4 }}>+{g.members.length-5}</span>}
                    </div>
                    <div style={{ display:"flex", justifyContent:"space-between", alignItems:"flex-end", borderTop:"1px solid rgba(0,255,255,.1)", paddingTop:12 }}>
                      <div>
                        <div className="lbl">Total</div>
                        <div className="ob" style={{ fontSize:15, color:"#ffff00", textShadow:"0 0 6px #ffff00" }}>₹{g.totalExpenses.toLocaleString()}</div>
                      </div>
                      <div style={{ textAlign:"right" }}>
                        <div className="lbl">Your balance</div>
                        <div className="ob" style={{ fontSize:14, fontWeight:700, color:g.myBalance>0?"#00ff88":g.myBalance<0?"#ff0066":"rgba(0,255,255,.3)",
                          textShadow:g.myBalance!==0?`0 0 8px ${g.myBalance>0?"#00ff88":"#ff0066"}`:"none" }}>
                          {g.myBalance===0?"Settled":g.myBalance>0?`+₹${g.myBalance}`:`−₹${Math.abs(g.myBalance)}`}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
          }
        </div>
      </div>

      {modal==="createGroup" && (
        <CreateGroupModal me={me} allUsers={allUsers}
          onClose={()=>setModal(null)}
          onCreate={g=>{setGroups(gs=>[g,...gs]);setModal(null);}}/>
      )}
      {modal==="editProfile" && (
        <EditProfileModal user={me}
          onClose={()=>setModal(null)}
          onSave={u=>{setMe(u);setModal(null);}}/>
      )}
    </div>
  );
}

/* ════════════════════════════════════════════════════════
   APP ROOT — Screen Router
════════════════════════════════════════════════════════ */
export default function App() {
  const [screen, setScreen] = useState("login"); // login | register | dashboard | group
  const [user, setUser] = useState(null);
  const [allUsers, setAllUsers] = useState([]);
  const [groupCtx, setGroupCtx] = useState(null); // { group, onUpdate }

  useEffect(() => { API.getUsers().then(setAllUsers); }, [screen]);

  const login = u => { setUser(u); setScreen("dashboard"); };
  const logout = () => { setUser(null); setScreen("login"); };

  const openGroup = (group, onUpdate) => {
    setGroupCtx({ group, onUpdate });
    setScreen("group");
  };

  return (
    <>
      <Styles/>
      {screen==="login"     && <LoginScreen onLogin={login} onReg={()=>setScreen("register")}/>}
      {screen==="register"  && <RegisterScreen onDone={()=>setScreen("login")} onLogin={()=>setScreen("login")}/>}
      {screen==="dashboard" && <Dashboard user={user} allUsers={allUsers} onLogout={logout} onOpenGroup={openGroup}/>}
      {screen==="group"     && groupCtx && (
        <GroupView
          group={groupCtx.group} me={user} allUsers={allUsers}
          onBack={()=>setScreen("dashboard")}
          onUpdate={g=>{ groupCtx.onUpdate(g); setGroupCtx(c=>({...c,group:g})); }}/>
      )}
    </>
  );
}







