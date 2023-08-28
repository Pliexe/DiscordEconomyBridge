export enum NodeType {
    // Value definition
    DEFINE_FLOAT = 0,
    DEFINE_INT = 1,
    DEFINE_STRING = 2,
    DEFINE_BOOLEAN = 3,
    DEFINE_PLAYER = 4,
    DEFINE_DISCORD_USER = 5,

    // Arithmetic
    ADD = 10,

    // Events
    ON_COMMAND = 200,

    // LOGIC
    IF = 300,
    GREATER = 301,
    LESS = 302,
    EQUAL = 303,
    NOT_EQUAL = 304,
    GREATER_EQUAL = 305,
    LESS_EQUAL = 306,
    AND = 307,
    OR = 308,
    NOT = 309,

    IFGREATER = 310,
    IFLESS = 311,
    IFEQUAL = 312,
    IFNOTEQUAL = 313,
    IFGREATEREQUAL = 314,
    IFLESSEQUAL = 315,
    IFAND = 316,
    IFOR = 317,
    IFNOT = 318,

    // Discord
    // Actions
    DISCORD_SEND_MESSAGE = 400,
}

export interface GraphNode {
    position: [number, number];
    type: NodeType;
    id: string;
}

export interface DataGraphNdoe<T> extends GraphNode {
    data: T;
}

export interface Connection {
    source: { id: string, output: number };
    target: { id: string, input: number };
}


// Flow refers to the next node to execute so in this case 99% of nodes need it and some need even more like If since it has 2 flows (true or false)
export type NodeEdgeType = "flow" | "number" | "boolean" | "string" | "minecraft:player" | "discord:user" | "discord:channel" | "number[]" | "boolean[]" | "string[]";

export function GetTypeIcon(type: NodeEdgeType): string | undefined {
    switch (type) {
        case "flow":
            return " ➙ ";
        default:
            return undefined;
    };
}

export const EdgeColor = (type: NodeEdgeType): string => {
    switch (type) {
        case "flow":
            return "white";
        case "string":
        case "string[]":
            return "yellow";
        case "number":
        case "number[]":
            return "cyan";
        case "boolean":
        case "boolean[]":
            return "red";
        case "minecraft:player":
            return "green";
        case "discord:user":
            return "blue";
        case "discord:channel":
            return "purple";
    }
}

export const GetNodeName = (type: NodeType): string => {
    switch (type) {
        case NodeType.ADD: return "Addition";
        case NodeType.DEFINE_FLOAT: return "Float";
        case NodeType.DEFINE_INT: return "Int";
        case NodeType.DEFINE_STRING: return "String";
        case NodeType.DEFINE_BOOLEAN: return "Boolean";
        case NodeType.DEFINE_PLAYER: return "Player";
        case NodeType.DEFINE_DISCORD_USER: return "Discord User";
        case NodeType.ON_COMMAND: return "On Command";
        case NodeType.IF: return "If (Conditional)";
        case NodeType.GREATER: return "Greater Than";
        case NodeType.LESS: return "Less Than";
        case NodeType.EQUAL: return "Equal";
        case NodeType.GREATER_EQUAL: return "Greater Than or Equal";
        case NodeType.LESS_EQUAL: return "Less Than or Equal";
        case NodeType.NOT_EQUAL: return "Not Equal";
        case NodeType.AND: return "And";
        case NodeType.OR: return "Or";
        case NodeType.NOT: return "Not";
        case NodeType.IFGREATER: return "If Greater Than";
        case NodeType.IFLESS: return "If Less Than";
        case NodeType.IFEQUAL: return "If Equal";
        case NodeType.IFGREATEREQUAL: return "If Greater Than or Equal";
        case NodeType.IFLESSEQUAL: return "If Less Than or Equal";
        case NodeType.IFNOTEQUAL: return "If Not Equal";
        case NodeType.IFAND: return "If And";
        case NodeType.IFOR: return "If Or";
        case NodeType.IFNOT: return "If Not";
        

        case NodeType.DISCORD_SEND_MESSAGE: return "Send Message";

        default: return NodeType[type];
    }
}

export const nodeEdgeMap = new Map<NodeType, { category: string; in: { name?: string; type: NodeEdgeType }[]; out: { name?: string; type: NodeEdgeType }[] }>([
    [NodeType.ON_COMMAND, { category: "event", in: [], out: [{ type: "flow" }] }],
    
    [NodeType.DEFINE_FLOAT, { category: "storage", in: [], out: [{ type: "number" }] }],
    [NodeType.DEFINE_INT, { category: "storage", in: [], out: [{ type: "number" }] }],
    [NodeType.DEFINE_STRING, { category: "storage", in: [], out: [{ type: "string" }] }],
    [NodeType.DEFINE_BOOLEAN, { category: "storage", in: [], out: [{ type: "boolean" }] }],
    [NodeType.DEFINE_PLAYER, { category: "storage", in: [], out: [{ type: "minecraft:player" }] }],
    [NodeType.DEFINE_DISCORD_USER, { category: "storage", in: [], out: [{ type: "discord:user" }] }],
    
    [NodeType.ADD, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ type: "flow" }, { name: "a+b", type: "number" }] }],
    [NodeType.IF, { category: "logic", in: [{ type: "flow" }, { name: "condition", type: "boolean" }], out: [{ name: "True", type: "flow" }, { name: "False", type: "flow" }] }],

    [NodeType.AND, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "boolean" }, { name: "b", type: "boolean" }], out: [{ type: "flow" }, { name: "a&&b", type: "boolean" }] }],
    [NodeType.OR, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "boolean" }, { name: "b", type: "boolean" }], out: [{ type: "flow" }, { name: "a||b", type: "boolean" }] }],
    [NodeType.NOT, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "boolean" }], out: [{ type: "flow" }, { name: "!a", type: "boolean" }] }],
    [NodeType.IFGREATER, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ name: "True", type: "flow" }, { name: "False", type: "flow" }] }],
    [NodeType.IFLESS, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ name: "True", type: "flow" }, { name: "False", type: "flow" }] }],
    [NodeType.IFEQUAL, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ name: "True", type: "flow" }, { name: "False", type: "flow" }] }],
    [NodeType.IFNOTEQUAL, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ name: "True", type: "flow" }, { name: "False", type: "flow" }] }],
    [NodeType.IFGREATEREQUAL, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ name: "True", type: "flow" }, { name: "False", type: "flow" }] }],
    [NodeType.IFLESSEQUAL, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ name: "True", type: "flow" }, { name: "False", type: "flow" }] }],
    [NodeType.IFAND, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "boolean" }, { name: "b", type: "boolean" }], out: [{ name: "True", type: "flow" }, { name: "False", type: "flow" }] }],
    [NodeType.IFOR, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "boolean" }, { name: "b", type: "boolean" }], out: [{ name: "True", type: "flow" }, { name: "False", type: "flow" }] }],
    [NodeType.IFNOT, { category: "logic", in: [{ type: "flow" }, { name: "a", type: "boolean" }], out: [{ name: "True", type: "flow" }, { name: "False", type: "flow" }] }],

    [NodeType.GREATER, { category: "comparison", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ type: "flow" }, { name: "a>b", type: "boolean" }] }],
    [NodeType.LESS, { category: "comparison", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ type: "flow" }, { name: "a<b", type: "boolean" }] }],
    [NodeType.EQUAL, { category: "comparison", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ type: "flow" }, { name: "a==b", type: "boolean" }] }],
    [NodeType.NOT_EQUAL, { category: "comparison", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ type: "flow" }, { name: "a!=b", type: "boolean" }] }],
    [NodeType.GREATER_EQUAL, { category: "comparison", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ type: "flow" }, { name: "a>=b", type: "boolean" }] }],
    [NodeType.LESS_EQUAL, { category: "comparison", in: [{ type: "flow" }, { name: "a", type: "number" }, { name: "b", type: "number" }], out: [{ type: "flow" }, { name: "a<=b", type: "boolean" }] }],
    
    [NodeType.DISCORD_SEND_MESSAGE, { 
        category: "discord",
        in: [{ type: "flow" }, { name: "Empherial", type: "boolean" }, { name: "Buttons", type: "string[]" }, { name: "Channel", type: "discord:channel" }, { name: "Message", type: "string" }],
        out: [{ type: "flow" }, { type: "flow", name: "On Action" }, { type: "flow", name: "On Edit" }] 
    }],
]);

export function getEdgeInputType(type: NodeType, edge: number): NodeEdgeType | undefined {
    return nodeEdgeMap.get(type)?.in[edge].type;
}



export function getEdgeOutputType(type: NodeType, edge: number): NodeEdgeType | undefined {
    return nodeEdgeMap.get(type)?.out[edge].type;
}