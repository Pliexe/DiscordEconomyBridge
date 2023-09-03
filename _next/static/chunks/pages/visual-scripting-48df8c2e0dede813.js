(self.webpackChunk_N_E=self.webpackChunk_N_E||[]).push([[680],{8327:function(e,n,t){(window.__NEXT_P=window.__NEXT_P||[]).push(["/visual-scripting",function(){return t(5814)}])},5814:function(e,n,t){"use strict";t.r(n),t.d(n,{default:function(){return F}});var o,a,r=t(5893),i=t(9650),c=t.n(i),l=t(7294),s=t(7632);function d(e){if("flow"===e)return" ➙ "}(o=a||(a={}))[o.DEFINE_FLOAT=0]="DEFINE_FLOAT",o[o.DEFINE_INT=1]="DEFINE_INT",o[o.DEFINE_STRING=2]="DEFINE_STRING",o[o.DEFINE_BOOLEAN=3]="DEFINE_BOOLEAN",o[o.DEFINE_PLAYER=4]="DEFINE_PLAYER",o[o.DEFINE_DISCORD_USER=5]="DEFINE_DISCORD_USER",o[o.ADD=10]="ADD",o[o.ON_COMMAND=200]="ON_COMMAND",o[o.IF=300]="IF",o[o.GREATER=301]="GREATER",o[o.LESS=302]="LESS",o[o.EQUAL=303]="EQUAL",o[o.NOT_EQUAL=304]="NOT_EQUAL",o[o.GREATER_EQUAL=305]="GREATER_EQUAL",o[o.LESS_EQUAL=306]="LESS_EQUAL",o[o.AND=307]="AND",o[o.OR=308]="OR",o[o.NOT=309]="NOT",o[o.IFGREATER=310]="IFGREATER",o[o.IFLESS=311]="IFLESS",o[o.IFEQUAL=312]="IFEQUAL",o[o.IFNOTEQUAL=313]="IFNOTEQUAL",o[o.IFGREATEREQUAL=314]="IFGREATEREQUAL",o[o.IFLESSEQUAL=315]="IFLESSEQUAL",o[o.IFAND=316]="IFAND",o[o.IFOR=317]="IFOR",o[o.IFNOT=318]="IFNOT",o[o.DISCORD_SEND_MESSAGE=400]="DISCORD_SEND_MESSAGE";let u=e=>{switch(e){case"flow":return"white";case"string":case"string[]":return"yellow";case"number":case"number[]":return"cyan";case"boolean":case"boolean[]":return"red";case"minecraft:player":return"green";case"discord:user":return"blue";case"discord:channel":return"purple"}},m=e=>{switch(e){case a.ADD:return"Addition";case a.DEFINE_FLOAT:return"Float";case a.DEFINE_INT:return"Int";case a.DEFINE_STRING:return"String";case a.DEFINE_BOOLEAN:return"Boolean";case a.DEFINE_PLAYER:return"Player";case a.DEFINE_DISCORD_USER:return"Discord User";case a.ON_COMMAND:return"On Command";case a.IF:return"If (Conditional)";case a.GREATER:return"Greater Than";case a.LESS:return"Less Than";case a.EQUAL:return"Equal";case a.GREATER_EQUAL:return"Greater Than or Equal";case a.LESS_EQUAL:return"Less Than or Equal";case a.NOT_EQUAL:return"Not Equal";case a.AND:return"And";case a.OR:return"Or";case a.NOT:return"Not";case a.IFGREATER:return"If Greater Than";case a.IFLESS:return"If Less Than";case a.IFEQUAL:return"If Equal";case a.IFGREATEREQUAL:return"If Greater Than or Equal";case a.IFLESSEQUAL:return"If Less Than or Equal";case a.IFNOTEQUAL:return"If Not Equal";case a.IFAND:return"If And";case a.IFOR:return"If Or";case a.IFNOT:return"If Not";case a.DISCORD_SEND_MESSAGE:return"Send Message";default:return a[e]}},p=new Map([[a.ON_COMMAND,{category:"event",in:[],out:[{type:"flow"}]}],[a.DEFINE_FLOAT,{category:"storage",in:[],out:[{type:"number"}]}],[a.DEFINE_INT,{category:"storage",in:[],out:[{type:"number"}]}],[a.DEFINE_STRING,{category:"storage",in:[],out:[{type:"string"}]}],[a.DEFINE_BOOLEAN,{category:"storage",in:[],out:[{type:"boolean"}]}],[a.DEFINE_PLAYER,{category:"storage",in:[],out:[{type:"minecraft:player"}]}],[a.DEFINE_DISCORD_USER,{category:"storage",in:[],out:[{type:"discord:user"}]}],[a.ADD,{category:"logic",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{type:"flow"},{name:"a+b",type:"number"}]}],[a.IF,{category:"logic",in:[{type:"flow"},{name:"condition",type:"boolean"}],out:[{name:"True",type:"flow"},{name:"False",type:"flow"}]}],[a.AND,{category:"logic",in:[{type:"flow"},{name:"a",type:"boolean"},{name:"b",type:"boolean"}],out:[{type:"flow"},{name:"a&&b",type:"boolean"}]}],[a.OR,{category:"logic",in:[{type:"flow"},{name:"a",type:"boolean"},{name:"b",type:"boolean"}],out:[{type:"flow"},{name:"a||b",type:"boolean"}]}],[a.NOT,{category:"logic",in:[{type:"flow"},{name:"a",type:"boolean"}],out:[{type:"flow"},{name:"!a",type:"boolean"}]}],[a.IFGREATER,{category:"logic",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{name:"True",type:"flow"},{name:"False",type:"flow"}]}],[a.IFLESS,{category:"logic",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{name:"True",type:"flow"},{name:"False",type:"flow"}]}],[a.IFEQUAL,{category:"logic",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{name:"True",type:"flow"},{name:"False",type:"flow"}]}],[a.IFNOTEQUAL,{category:"logic",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{name:"True",type:"flow"},{name:"False",type:"flow"}]}],[a.IFGREATEREQUAL,{category:"logic",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{name:"True",type:"flow"},{name:"False",type:"flow"}]}],[a.IFLESSEQUAL,{category:"logic",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{name:"True",type:"flow"},{name:"False",type:"flow"}]}],[a.IFAND,{category:"logic",in:[{type:"flow"},{name:"a",type:"boolean"},{name:"b",type:"boolean"}],out:[{name:"True",type:"flow"},{name:"False",type:"flow"}]}],[a.IFOR,{category:"logic",in:[{type:"flow"},{name:"a",type:"boolean"},{name:"b",type:"boolean"}],out:[{name:"True",type:"flow"},{name:"False",type:"flow"}]}],[a.IFNOT,{category:"logic",in:[{type:"flow"},{name:"a",type:"boolean"}],out:[{name:"True",type:"flow"},{name:"False",type:"flow"}]}],[a.GREATER,{category:"comparison",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{type:"flow"},{name:"a>b",type:"boolean"}]}],[a.LESS,{category:"comparison",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{type:"flow"},{name:"a<b",type:"boolean"}]}],[a.EQUAL,{category:"comparison",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{type:"flow"},{name:"a==b",type:"boolean"}]}],[a.NOT_EQUAL,{category:"comparison",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{type:"flow"},{name:"a!=b",type:"boolean"}]}],[a.GREATER_EQUAL,{category:"comparison",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{type:"flow"},{name:"a>=b",type:"boolean"}]}],[a.LESS_EQUAL,{category:"comparison",in:[{type:"flow"},{name:"a",type:"number"},{name:"b",type:"number"}],out:[{type:"flow"},{name:"a<=b",type:"boolean"}]}],[a.DISCORD_SEND_MESSAGE,{category:"discord",in:[{type:"flow"},{name:"Empherial",type:"boolean"},{name:"Buttons",type:"string[]"},{name:"Channel",type:"discord:channel"},{name:"Message",type:"string"}],out:[{type:"flow"},{type:"flow",name:"On Action"},{type:"flow",name:"On Edit"}]}]]);function y(e,n){var t;return null===(t=p.get(e))||void 0===t?void 0:t.in[n].type}var g=t(505),h=t.n(g),E=t(1193);function f(e){var n,t;return"in"===e.direction?(0,r.jsx)(r.Fragment,{children:(0,r.jsxs)("text",{fontSize:"".concat(e.scale,"rem"),x:e.position[0],y:e.position[1],fill:"white",children:[(0,r.jsx)("tspan",{cursor:"pointer",id:"".concat(e.nodeId,"_i").concat(e.edge),fill:u(e.type),children:"flow"==e.type?e.connected?"▶":"▷":e.connected?"⚫":"⚪"}),"\xa0\xa0",(0,r.jsxs)("tspan",{cursor:"pointer",fill:u(e.type),children:[null!==(n=e.icon)&&void 0!==n?n:"⚫"," "]}),e.text]})}):(0,r.jsx)(r.Fragment,{children:(0,r.jsxs)("text",{fontSize:"".concat(e.scale,"rem"),textAnchor:"end",x:e.position[0],y:e.position[1],fill:"white",children:[e.text,"\xa0",(0,r.jsx)("tspan",{cursor:"pointer",fill:u(e.type),children:null!==(t=e.icon)&&void 0!==t?t:"⚫"}),"\xa0\xa0",(0,r.jsx)("tspan",{cursor:"pointer",id:"".concat(e.nodeId,"_o").concat(e.edge),fill:u(e.type),children:e.connected?"⚫":"⚪"})]})})}var v=t(4337),x=t.n(v);function b(e){return(0,r.jsx)("foreignObject",{x:e.x,y:e.y,width:e.width,height:e.height,children:(0,r.jsx)("input",{className:x().input,type:"text",placeholder:e.placeholder,value:e.value,onChange:n=>{var t;return null===(t=e.onChange)||void 0===t?void 0:t.call(e,n.target.value)}})})}var _=t(6958),N=t.n(_);function I(e){var n;return(0,r.jsx)("foreignObject",{x:e.x,y:e.y,width:e.width,height:e.height,children:(0,r.jsxs)("div",{className:N().bool,children:[(0,r.jsxs)("div",{children:[(0,r.jsx)("label",{children:"True"}),(0,r.jsx)("input",{onClick:()=>{e.onChange&&e.onChange(!0)},type:"radio",name:"True",id:"",checked:null!==(n=e.value)&&void 0!==n&&n})]}),(0,r.jsxs)("div",{children:[(0,r.jsx)("label",{children:"False"}),(0,r.jsx)("input",{onClick:()=>{e.onChange&&e.onChange(!1)},type:"radio",name:"False",id:"",checked:void 0!=e.value&&!e.value})]})]})})}let j=(e,n,t,o)=>"in"==t?[-20*o,(50+30*n)*o]:[(A(e.type)[0]+20)*o,(50+30*n)*o],A=e=>{var n,t,o,r,i,c,l,s,d,u,y,g,h,E;if(e==a.DEFINE_BOOLEAN||e==a.DEFINE_INT||e==a.DEFINE_STRING||e==a.DEFINE_FLOAT||e==a.DEFINE_PLAYER||e==a.DEFINE_DISCORD_USER)return e==a.DEFINE_STRING?[200,100]:[200,50+30*Math.max(null!==(l=null===(i=p.get(e))||void 0===i?void 0:i.in.length)&&void 0!==l?l:0,null!==(s=null===(c=p.get(e))||void 0===c?void 0:c.out.length)&&void 0!==s?s:0)];let f=(null!==(u=null===(n=p.get(e))||void 0===n?void 0:n.in.reduce((e,n)=>{var t;return(null!==(d=null===(t=n.name)||void 0===t?void 0:t.length)&&void 0!==d?d:0)>e.length?n.name:e},"").length)&&void 0!==u?u:0)+(null!==(g=null===(t=p.get(e))||void 0===t?void 0:t.out.reduce((e,n)=>{var t;return(null!==(y=null===(t=n.name)||void 0===t?void 0:t.length)&&void 0!==y?y:0)>e.length?n.name:e},"").length)&&void 0!==g?g:0),v=m(e);return[50+(f=Math.max(f,v.length))/Math.log(f)*30,50+30*Math.max(null!==(h=null===(o=p.get(e))||void 0===o?void 0:o.in.length)&&void 0!==h?h:0,null!==(E=null===(r=p.get(e))||void 0===r?void 0:r.out.length)&&void 0!==E?E:0)]},w=e=>{var n,t,o,i;let{key:c,node:s,parentOffset:u,boardOffset:y,scale:g,highlight:v,setNode:x,connections:_,onKeyUp:N,onNodeClick:w}=e,[F,D]=(0,l.useState)([0,0]);return(0,r.jsx)(r.Fragment,{children:(0,r.jsx)(E.DraggableCore,{onStart:(e,n)=>{D([n.x-n.node.getBoundingClientRect().x*(1/g),n.y-n.node.getBoundingClientRect().y*(1/g)])},onDrag:(e,n)=>{x({...s,position:[n.x-y[0]*(1/g)-F[0],n.y-y[1]*(1/g)-F[1]]})},offsetParent:u,scale:g,children:(0,r.jsxs)("g",{x:s.position[0]*g+y[0],y:s.position[1]*g+y[1],transform:"translate(".concat(s.position[0]*g+y[0],", ").concat(s.position[1]*g+y[1],")"),children:[(0,r.jsx)("rect",{onMouseUp:w,onKeyUp:N,className:v?"".concat(h().node," ").concat(h().highlight):h().node,width:A(s.type)[0]*g,height:A(s.type)[1]*g,fill:"rgba(0, 0, 0, 0.5)",stroke:"white",strokeWidth:"5",rx:"10",ry:"10"}),(0,r.jsx)("text",{textAnchor:"middle",fontWeight:"bold",fontSize:"".concat(g,"rem"),x:A(s.type)[0]*g/2,y:20*g,children:m(s.type)}),null!==(o=null===(n=p.get(s.type))||void 0===n?void 0:n.in.map((e,n)=>{var t;return(0,r.jsx)(f,{scale:g,nodeId:s.id,edge:n,position:j(s,n,"in",g),type:e.type,text:null!==(t=e.name)&&void 0!==t?t:"",direction:"in",icon:d(e.type),connected:_.some(e=>e.target.id==s.id&&e.target.input==n)})}))&&void 0!==o?o:null,null!==(i=null===(t=p.get(s.type))||void 0===t?void 0:t.out.map((e,n)=>{var t;return(0,r.jsx)(f,{scale:g,nodeId:s.id,edge:n,position:j(s,n,"out",g),direction:"out",icon:d(e.type),connected:_.some(e=>e.source.id==s.id&&e.source.output==n),type:e.type,text:null!==(t=e.name)&&void 0!==t?t:""})}))&&void 0!==i?i:null,(()=>{switch(s.type){case a.DEFINE_STRING:return(0,r.jsx)(b,{x:5,y:60*g,width:(A(s.type)[0]-10)*g,height:30*g,value:s.data,placeholder:"Enter a string",onChange:e=>{x({...s,data:e})}});case a.DEFINE_BOOLEAN:return(0,r.jsx)(I,{x:5,y:35*g,width:(A(s.type)[0]-10)*g,height:20*g,value:s.data,onChange:e=>{x({...s,data:e})}})}})()]},c)},c)})};function F(){let e=(0,l.useRef)(null),n=(0,l.useRef)(null),[t,o]=(0,l.useState)(1),[i,d]=(0,l.useState)(null),[g,h]=(0,l.useState)([0,0]),[E,f]=(0,l.useState)(void 0),[v,x]=(0,l.useState)(void 0),[b,_]=(0,l.useState)(),[N,I]=(0,l.useState)({x:0,y:0}),A=e=>{if(null==F||void 0===b||void 0===F.commands[b])return;if(!e.target.id){d(null);return}let n=e.target.id;if(!Object.keys(F.commands[b].nodes).some(e=>n.startsWith(e))&&!/^(([A-z]|\d|-)*_[io]\d*)$/.test(n)){null!==i&&d(null);return}let[t,o]=n.split("_"),a=o[0];console.log("Click on "+t+" "+(o=o.slice(1))+" "+a),null===i&&"o"==a?d({source:{id:t,output:parseInt(o)},target:{id:"",input:0}}):"i"==a&&null!==i&&L(F.commands[b].nodes[i.source.id],i.source.output,F.commands[b].nodes[t],parseInt(o))},[F,D]=(0,l.useState)({commands:{}});if(null===F)return(0,r.jsx)("main",{children:(0,r.jsxs)("div",{className:"bg-midnight flex h-100vh items-center",children:[(0,r.jsx)("h1",{children:"Import configuration! ;)"}),(0,r.jsx)("button",{onClick:()=>D({commands:{}}),children:"Create New"})]})});let S=e=>{void 0!==b&&void 0!==F.commands[b]&&D({...F,commands:{...F.commands,[b]:{...F.commands[b],nodes:Object.fromEntries(Object.entries(F.commands[b].nodes).filter(n=>{let[t,o]=n;return t!==e})),connections:F.commands[b].connections.filter(n=>!(n.source.id===e||n.target.id===e))}}})},C=function(e){for(var n=arguments.length,t=Array(n>1?n-1:0),o=1;o<n;o++)t[o-1]=arguments[o];if(console.log("Add node "+e+" at position "+t.toString()),console.log("Condition: "+(void 0===b)),console.log("Condition: "+(void 0===F.commands[b])),void 0===b||void 0===F.commands[b])return;let a=()=>{let e=(0,s.Z)();return void 0===F.commands[b].nodes[e]?e:a()},r=a();console.log("Create new node with id "+r);let i={position:t,type:e,id:r};return D({...F,commands:{...F.commands,[b]:{...F.commands[b],nodes:{...F.commands[b].nodes,[r]:i}}}}),i},O=(e,n,t)=>{void 0!==b&&void 0!==F.commands[b]&&("in"===t?D({...F,commands:{...F.commands,[b]:{...F.commands[b],connections:F.commands[b].connections.filter(t=>!(t.target.id===e.id&&t.target.input===n))}}}):D({...F,commands:{...F.commands,[b]:{...F.commands[b],connections:F.commands[b].connections.filter(t=>!(t.source.id===e.id&&t.source.output===n))}}}))},L=(e,n,t,o)=>{var a,r;if(void 0===b||void 0===F.commands[b]||e.id===t.id||(a=e.type,(null===(r=p.get(a))||void 0===r?void 0:r.out[n].type)!==y(t.type,o)))return;d(null);let i={source:{id:e.id,output:n},target:{id:t.id,input:o}};F.commands[b].connections.some(e=>e.target.id===t.id&&e.target.input===o)?F.commands[b].connections.some(t=>t.source.id===e.id&&t.source.output===n)?O(e,n,"out"):D({...F,commands:{...F.commands,[b]:{...F.commands[b],connections:F.commands[b].connections.map(e=>e.target.id===t.id&&e.target.input===o?i:e)}}}):D({...F,commands:{...F.commands,[b]:{...F.commands[b],connections:[...F.commands[b].connections,i]}}})},T=()=>{let e=prompt("Name of the command?");if(void 0==e)return;let n=prompt("Description of the command?");if(null===n)return;_(e);let t={...F,commands:{...F.commands,[e]:{description:n,options:{},nodes:{oncommand:{position:[150,150],type:a.ON_COMMAND,id:"oncommand"},add:{position:[450,150],type:a.ADD,id:"add"}},connections:[{source:{id:"oncommand",output:0},target:{id:"add",input:0}}]}}};D(t)},R=(0,l.useRef)(!1),k=(0,l.useRef)({x:0,y:0}),U=e=>{if("Delete"===e.key||"Backspace"===e.key){var n;(null===(n=document.activeElement)||void 0===n?void 0:n.tagName)!=="INPUT"&&void 0!==E&&(void 0!==v?(O(E,v,"out"),x(void 0)):S(E.id),f(void 0))}};return(0,l.useEffect)(()=>(document.addEventListener("click",A),document.addEventListener("keyup",U),()=>{document.removeEventListener("click",A),document.removeEventListener("keyup",U)}),[A,e,U]),(0,r.jsx)("main",{children:(0,r.jsxs)("div",{className:c().editConfig,onMouseMove:e=>{I({x:e.clientX,y:e.clientY})},children:[(0,r.jsxs)("div",{className:c().commandsList,children:[(0,r.jsxs)("div",{children:[(0,r.jsx)("h1",{children:"Color Legend"}),(0,r.jsxs)("ul",{children:[(0,r.jsxs)("li",{children:[(0,r.jsx)("span",{style:{color:u("flow")},children:u("flow")})," - Flow"]}),(0,r.jsxs)("li",{children:[(0,r.jsx)("span",{style:{color:u("number")},children:u("number")})," - Number"]}),(0,r.jsxs)("li",{children:[(0,r.jsx)("span",{style:{color:u("boolean")},children:u("boolean")})," - Boolean"]}),(0,r.jsxs)("li",{children:[(0,r.jsx)("span",{style:{color:u("string")},children:u("string")})," - String"]}),(0,r.jsxs)("li",{children:[(0,r.jsx)("span",{style:{color:u("minecraft:player")},children:u("minecraft:player")})," Minecraft - Player"]}),(0,r.jsxs)("li",{children:[(0,r.jsx)("span",{style:{color:u("discord:user")},children:u("discord:user")})," Discord - User"]}),(0,r.jsxs)("li",{children:[(0,r.jsx)("span",{style:{color:u("discord:channel")},children:u("discord:channel")})," Discord - Channel"]})]})]}),(0,r.jsxs)("div",{children:[(0,r.jsx)("h1",{children:"Global Values"}),(0,r.jsx)("button",{children:"Add Value"})]}),(0,r.jsx)("hr",{}),(0,r.jsxs)("div",{children:[(0,r.jsx)("h1",{children:"Commands"}),Object.keys(F.commands).map(e=>(0,r.jsx)("div",{onClick:()=>_(e),className:c().command,children:(0,r.jsx)("h2",{children:e})},e)),(0,r.jsx)("button",{onClick:()=>T(),children:"Add Command"})]}),(0,r.jsx)("hr",{}),(0,r.jsxs)("div",{children:[(0,r.jsx)("button",{onClick:()=>{var e;null===(e=document.getElementsByClassName(c().exportedConf).item(0))||void 0===e||e.classList.toggle(c().open)},children:"Export"}),(0,r.jsx)("button",{onClick:()=>{var e;let n=document.getElementById("impInput");void 0!==n&&(n.value=JSON.stringify(F,null,4),null===(e=document.getElementsByClassName(c().importConf).item(0))||void 0===e||e.classList.toggle(c().open))},children:"Import"}),(0,r.jsx)("div",{className:c().exportedConf,children:(0,r.jsxs)("div",{children:[(0,r.jsx)("h1",{children:"Exported Configuration"}),(0,r.jsx)("pre",{className:"language-javascript",children:(0,r.jsx)("p",{className:c().codeblock,ref:n,children:(0,r.jsx)("code",{children:JSON.stringify(F,null,4)})})}),(0,r.jsxs)("div",{children:[(0,r.jsx)("button",{onClick:()=>{let e=document.createElement("textarea");e.value=JSON.stringify(F,null,4),document.body.appendChild(e),e.select(),document.execCommand("copy"),document.body.removeChild(e)},children:"Copy"}),(0,r.jsx)("button",{onClick:()=>{var e;return null===(e=document.getElementsByClassName(c().exportedConf).item(0))||void 0===e?void 0:e.classList.toggle(c().open)},children:"Close"})]})]})}),(0,r.jsx)("div",{className:c().importConf,children:(0,r.jsxs)("div",{children:[(0,r.jsx)("h1",{children:"Import Configuration"}),(0,r.jsx)("pre",{className:"language-javascript",children:(0,r.jsx)("p",{className:c().codeblock,ref:n,children:(0,r.jsx)("code",{children:(0,r.jsx)("textarea",{id:"impInput",defaultValue:JSON.stringify(F,null,4)})})})}),(0,r.jsxs)("div",{children:[(0,r.jsx)("button",{onClick:()=>{let e=document.getElementById("impInput");if(void 0!==e)try{let n=JSON.parse(e.value);D(n),alert("Imported!")}catch(e){alert("Invalid JSON!")}},children:"Import"}),(0,r.jsx)("button",{onClick:()=>{var e;return null===(e=document.getElementsByClassName(c().importConf).item(0))||void 0===e?void 0:e.classList.toggle(c().open)},children:"Close"})]})]})})]})]}),(0,r.jsx)("div",{className:c().board,children:void 0===F.commands[b]?(0,r.jsxs)("div",{className:c().noSelected,children:[(0,r.jsx)("h1",{children:"No command selected"}),(0,r.jsx)("p",{children:"Click the button bellow to create a new command!"}),(0,r.jsx)("button",{onClick:()=>T(),children:"Create a new command!"})]}):(0,r.jsx)("svg",{cursor:!0==R.current?"grabbing":"cursor",ref:e,preserveAspectRatio:"none",onDrag:e=>{h([g[0]+e.movementX,g[1]+e.movementY]),alert("Drag")},onMouseDown:e=>{f(void 0),1===e.button&&(R.current=!0,k.current={x:e.clientX,y:e.clientY})},onMouseMove:e=>{if(R.current){let n=e.clientX-k.current.x,t=e.clientY-k.current.y;h(e=>[e[0]+n,e[1]+t]),k.current={x:e.clientX,y:e.clientY}}},onMouseUp:()=>{R.current=!1},onWheel:e=>{o(t+-.001*e.deltaY)},children:(0,r.jsxs)("g",{children:[F.commands[b].connections.map((e,n)=>{var o;let a=F.commands[b].nodes[e.source.id],i=F.commands[b].nodes[e.target.id];if(void 0===a||void 0===i)return null;let l=j(a,e.source.output,"out",t),s=j(i,e.target.input,"in",t),d=[a.position[0]*t+g[0]+l[0]-2*t,a.position[1]*t+g[1]+l[1]-5*t],m=[i.position[0]*t+g[0]+s[0]+2*t,i.position[1]*t+g[1]+s[1]-5*t];return(0,r.jsxs)("g",{children:["selectedNode          ",(0,r.jsx)("path",{onClick:n=>{f(a),x(e.source.output)},d:"M ".concat(d[0]," ").concat(d[1]," C ").concat(d[0]+50," ").concat(d[1]," ").concat(m[0]-50," ").concat(m[1]," ").concat(m[0]," ").concat(m[1]),stroke:(null==E?void 0:E.id)===a.id&&v===e.source.output?"red":"transparent",strokeWidth:"10",strokeDasharray:"flow"===y(i.type,e.target.input)?"10,5":"",cursor:"pointer",fill:"transparent",filter:(null==E?void 0:E.id)===a.id&&v===e.source.output?"drop-shadow(0 0 0.25rem rgba(0,0,0, 255))":""},n),(0,r.jsx)("path",{onClick:n=>{f(a),x(e.source.output)},d:"M ".concat(d[0]," ").concat(d[1]," C ").concat(d[0]+50," ").concat(d[1]," ").concat(m[0]-50," ").concat(m[1]," ").concat(m[0]," ").concat(m[1]),stroke:"transparent",strokeWidth:"25",cursor:"pointer",fill:"transparent"},n),(0,r.jsx)("path",{onClick:n=>{f(a),x(e.source.output)},d:"M ".concat(d[0]," ").concat(d[1]," C ").concat(d[0]+50," ").concat(d[1]," ").concat(m[0]-50," ").concat(m[1]," ").concat(m[0]," ").concat(m[1]),stroke:u(null!==(o=y(i.type,e.target.input))&&void 0!==o?o:"flow"),strokeWidth:"5",strokeDasharray:"flow"===y(i.type,e.target.input)?"10,5":"",cursor:"pointer",fill:"transparent",filter:(null==E?void 0:E.id)===a.id&&v===e.source.output?"drop-shadow(0 0 0.25rem rgba(0,0,0, 255))":"",className:"flow"===y(i.type,e.target.input)?c().stroke:""},n)]},n)}),null===i?null:(()=>{let e=F.commands[b].nodes[i.source.id],n=document.getElementById("".concat(e.id,"_o").concat(i.source.output));if(null===n)return null;let t=n.getBoundingClientRect(),o=[t.x+t.width/2,t.y+t.height/2],a=[N.x,N.y];return(0,r.jsx)("path",{style:{pointerEvents:"none"},d:"M ".concat(o[0]," ").concat(o[1]," C ").concat(o[0]+50," ").concat(o[1]," ").concat(a[0]-50," ").concat(a[1]," ").concat(a[0]," ").concat(a[1]),stroke:"white",strokeWidth:"5",fill:"transparent",strokeDasharray:"10,5"})})(),Object.entries(F.commands[b].nodes).map(n=>{let[o,a]=n;return(0,r.jsx)("g",{children:(0,r.jsx)(w,{onNodeClick:e=>{e.stopPropagation(),f(a),x(void 0)},node:a,scale:t,boardOffset:g,connections:F.commands[b].connections,setNode:e=>{D({...F,commands:{...F.commands,[b]:{...F.commands[b],nodes:{...F.commands[b].nodes,[o]:e}}}})},parentOffset:e.current,highlight:(null==E?void 0:E.id)===a.id&&void 0===v},o)},o)})]})})}),(0,r.jsx)("div",{className:c().nodes,children:Array.from(p.entries()).map(e=>{let[n,t]=e;return t.category}).filter((e,n,t)=>t.indexOf(e)===n).map(e=>(0,r.jsxs)("div",{className:"".concat(c().section," ").concat(c().open),children:[(0,r.jsx)("h1",{onClick:e=>{var n,t,o;return(null===(n=e.currentTarget.parentElement)||void 0===n?void 0:n.classList.contains(c().open))?null===(t=e.currentTarget.parentElement)||void 0===t?void 0:t.classList.remove(c().open):null===(o=e.currentTarget.parentElement)||void 0===o?void 0:o.classList.add(c().open)},children:e}),Array.from(p.entries()).filter(n=>{let[t,o]=n;return o.category===e}).map(e=>{let[n,t]=e;return(0,r.jsx)("button",{onClick:()=>C(n,150,150),children:m(n)})})]}))})]})})}t(5660)},6958:function(e){e.exports={bool:"edit_boolean_bool__O63fF"}},4337:function(e){e.exports={input:"edit_string_input__6kFBA"}},505:function(e){e.exports={node:"node_node__9wEPp",highlight:"node_highlight__cv0UJ"}},9650:function(e){e.exports={editConfig:"pluginconfig_editConfig__rXia0",commandsList:"pluginconfig_commandsList__eVP2_",command:"pluginconfig_command__Lebnl",exportedConf:"pluginconfig_exportedConf__n6tuW",importConf:"pluginconfig_importConf__isBn6",open:"pluginconfig_open__H78Ok",codeblock:"pluginconfig_codeblock___bfHX",board:"pluginconfig_board__dg_cL",stroke:"pluginconfig_stroke__y8HaO",dash:"pluginconfig_dash__r84Cq",noSelected:"pluginconfig_noSelected__2Oru0",nodes:"pluginconfig_nodes__OdysL",section:"pluginconfig_section__cxxWx"}}},function(e){e.O(0,[164,774,888,179],function(){return e(e.s=8327)}),_N_E=e.O()}]);