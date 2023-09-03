import Style from "../styles/pages/pluginconfig.module.scss";
import { KeyboardEvent, MouseEventHandler, useEffect, useRef, useState } from "react";
import Draggable, { DraggableCore } from "react-draggable";
import { v4 as uuidv4 } from "uuid";
import { Connection, EdgeColor, GetNodeName, GraphNode, NodeType, getEdgeInputType, getEdgeOutputType, nodeEdgeMap } from "@/interfaces/graph";
import { GetEdgePosition, Node } from "@/components/node";
import Prism from 'prismjs';

// export interface Node {
//     location: [number, number];
//     type: NodeType;
//     next: number | null;
// }

export interface Configuration {
    commands: {
        [command: string]: {
            description: string;
            options: {
                [option: string]: {
                    name: string,
                    description: string,
                    type: "string" | "number" | "boolean" | "string[]" | "number[]" | "boolean[]",
                    required: boolean,
                    default: string | number | boolean | string[] | number[] | boolean[]
                }
            };
            nodes: { [id: string]: GraphNode; };
            connections: Connection[];
        }
    }
}

export default function EditConfig() {
    const svgRef = useRef<SVGSVGElement>(null);

    const customCommandsConfDivRef = useRef<HTMLDivElement>(null);

    const [boardScale, setBoardScale] = useState<number>(1);
    const [tmpEdgeSelected, setTmpEdgeSelected] = useState<Connection | null>(null);
    const [boardOffset, setBoardOffset] = useState<[number, number]>([0, 0]);

    const [selectedNode, setSelectedNode] = useState<GraphNode | undefined>(undefined);
    const [selectedNodeEdge, setSelectedNodeEdge] = useState<number | undefined>(undefined);
    const [selectedCommand, setSelectedCommand] = useState<string | undefined>();

    const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });

    const onClick = (event: MouseEvent) => {
        if (configuration == null) return;
        if (selectedCommand === undefined) return;
        if (configuration.commands[selectedCommand!!] === undefined) return;
        if (!(event.target as HTMLElement).id) {
            setTmpEdgeSelected(null);
            return;
        }

        const id = (event.target as HTMLElement).id;
        if (!Object.keys(configuration!!.commands[selectedCommand!!].nodes).some((key) => id.startsWith(key)) && !/^(([A-z]|\d|-)*_[io]\d*)$/.test(id)) {
            if (tmpEdgeSelected !== null) {
                setTmpEdgeSelected(null);
            }
            return;
        }
        let [nodeId, edge] = id.split("_")
        let direction = edge[0];
        edge = edge.slice(1);

        console.log("Click on " + nodeId + " " + edge + " " + direction);

        if (tmpEdgeSelected === null && direction == "o") {

            setTmpEdgeSelected({
                source: {
                    id: nodeId,
                    output: parseInt(edge)
                },
                target: {
                    id: "",
                    input: 0
                }
            });

        } else if (direction == "i" && tmpEdgeSelected !== null) {
            create_connection(
                configuration!!.commands[selectedCommand!!].nodes[tmpEdgeSelected!!.source.id], tmpEdgeSelected!!.source.output,
                configuration!!.commands[selectedCommand!!].nodes[nodeId], parseInt(edge)
            );
        }
    };



    // const [configuration, setConfiguration] = useState<Configuration | null>(null);
    const [configuration, setConfiguration] = useState<Configuration | null>({ commands: {} });

    if (configuration === null) {
        return (
            <main>
                <div className="bg-midnight flex h-100vh items-center">
                    <h1>Import configuration! ;)</h1>
                    <button onClick={() => setConfiguration({ commands: {} })} >Create New</button>
                </div>
            </main>
        )
    }

    const remove_node = (id: string): void => {
        if (selectedCommand === undefined) return;
        if (configuration.commands[selectedCommand!!] === undefined) return;
        setConfiguration({
            ...configuration, commands:
            {
                ...configuration.commands,
                [selectedCommand!!]:
                {
                    ...configuration.commands[selectedCommand!!],
                    nodes: Object.fromEntries(Object.entries(configuration.commands[selectedCommand!!].nodes).filter(([key, value]) => key !== id)),
                    connections: configuration.commands[selectedCommand!!].connections.filter((connection) => {
                        return !(connection.source.id === id || connection.target.id === id);
                    })
                }
            }
        });
    }

    const add_node = (node: NodeType, ...position: [number, number]): GraphNode | undefined => {
        console.log("Add node " + node + " at position " + position.toString());
        console.log("Condition: " + (selectedCommand === undefined));
        console.log("Condition: " + (configuration.commands[selectedCommand!!] === undefined));
        if (selectedCommand === undefined) return undefined;
        if (configuration.commands[selectedCommand!!] === undefined) return undefined;
        const getNewId = (): string => { const id = uuidv4(); return configuration.commands[selectedCommand!!].nodes[id] === undefined ? id : getNewId(); }
        const newId = getNewId();
        console.log("Create new node with id " + newId);
        const newNode = {
            position,
            type: node,
            id: newId
        };
        setConfiguration({
            ...configuration, commands:
            {
                ...configuration.commands,
                [selectedCommand]:
                {
                    ...configuration.commands[selectedCommand],
                    nodes: {
                        ...configuration.commands[selectedCommand!!].nodes,
                        [newId]: newNode
                    }
                }
            }
        });
        return newNode;
    }

    const remove_connection = (source: GraphNode, edge: number, direction: "in" | "out"): void => {
        if (selectedCommand === undefined) return;
        if (configuration.commands[selectedCommand!!] === undefined) return;
        if (direction === "in") {
            setConfiguration({
                ...configuration, commands:
                {
                    ...configuration.commands,
                    [selectedCommand!!]:
                    {
                        ...configuration.commands[selectedCommand!!],
                        connections: configuration.commands[selectedCommand!!].connections.filter((connection) => {
                            return !(connection.target.id === source.id && connection.target.input === edge);
                        })
                    }
                }
            });
        } else {
            setConfiguration({
                ...configuration, commands:
                {
                    ...configuration.commands,
                    [selectedCommand!!]:
                    {
                        ...configuration.commands[selectedCommand!!],
                        connections: configuration.commands[selectedCommand!!].connections.filter((connection) => {
                            return !(connection.source.id === source.id && connection.source.output === edge);
                        })
                    }
                }
            });
        }
    }

    const create_connection = (source: GraphNode, output: number, target: GraphNode, input: number): void => {
        if (selectedCommand === undefined) return;
        if (configuration.commands[selectedCommand!!] === undefined) return;
        if (source.id === target.id) return;
        if (getEdgeOutputType(source.type, output) !== getEdgeInputType(target.type, input)) return;

        setTmpEdgeSelected(null);

        const newConnection: Connection = {
            source: {
                id: source.id,
                output
            },
            target: {
                id: target.id,
                input
            }
        };

        // If target and source are already connected, remove the old connection and dont add the new one. If the target already has a connection but the source doesnt match the new source then replace the old connection with the new one.
        if (configuration.commands[selectedCommand!!].connections.some((connection) => connection.target.id === target.id && connection.target.input === input)) {
            if (configuration.commands[selectedCommand!!].connections.some((connection) => connection.source.id === source.id && connection.source.output === output)) {
                remove_connection(source, output, "out");
            } else {
                setConfiguration({
                    ...configuration, commands:
                    {
                        ...configuration.commands,
                        [selectedCommand!!]:
                        {
                            ...configuration.commands[selectedCommand!!],
                            connections: configuration.commands[selectedCommand!!].connections.map((connection) => {
                                if (connection.target.id === target.id && connection.target.input === input) {
                                    return newConnection;
                                } else {
                                    return connection;
                                }
                            })
                        }
                    }
                });
            }
        }
        else {
            setConfiguration({
                ...configuration, commands:
                {
                    ...configuration.commands,
                    [selectedCommand!!]:
                    {
                        ...configuration.commands[selectedCommand!!],
                        connections: [...configuration.commands[selectedCommand!!].connections, newConnection]
                    }
                }
            });
        }
    }



    const add_commands = () => {
        const name = prompt("Name of the command?");
        if (name == undefined) return;
        const description = prompt("Description of the command?");
        if (description === null) return;
        setSelectedCommand(name);
        const tmp: Configuration = {
            ...configuration, commands:
            {
                ...configuration.commands,
                [name]:
                {
                    description,
                    options: {},
                    nodes: {
                        "oncommand": {
                            position: [150, 150],
                            type: NodeType.ON_COMMAND,
                            id: "oncommand"
                        },
                        "add": {
                            position: [450, 150],
                            type: NodeType.ADD,
                            id: "add"
                        }
                    },
                    connections: [{
                        source: {
                            id: "oncommand",
                            output: 0
                        },
                        target: {
                            id: "add",
                            input: 0
                        }
                    }]
                },
            }
        };
        setConfiguration(tmp);
    }

    const isDraggingRef = useRef(false);
    const startCoordsRef = useRef({ x: 0, y: 0 });

    const onKeyUp = (event: globalThis.KeyboardEvent) => {
        if (event.key === "Delete" || event.key === "Backspace") {

            if (document.activeElement?.tagName === "INPUT") return;

            if (selectedNode !== undefined) {
                if (selectedNodeEdge !== undefined) {
                    remove_connection(selectedNode, selectedNodeEdge, "out");
                    setSelectedNodeEdge(undefined);
                    setSelectedNode(undefined);
                } else {
                    remove_node(selectedNode.id);
                    setSelectedNode(undefined);
                }
            }
        }
    }

    useEffect(() => {
        document.addEventListener("click", (onClick));
        document.addEventListener("keyup", onKeyUp);

        return () => {
            document.removeEventListener("click", onClick);
            document.removeEventListener("keyup", onKeyUp);
        };

        if (typeof window !== 'undefined') {
            Prism.highlightAll();
        }
    }, [onClick, svgRef, onKeyUp]);

    return (
        <main>
            <div className={Style.editConfig} onMouseMove={(event) => {
                setMousePosition({ x: event.clientX, y: event.clientY });
            }} >

                <div className={Style.commandsList}>
                    <div>
                        <h1>Color Legend</h1>
                        <ul>
                            <li><span style={{ color: EdgeColor("flow") }}>{EdgeColor("flow")}</span> - Flow</li>
                            <li><span style={{ color: EdgeColor("number") }}>{EdgeColor("number")}</span> - Number</li>
                            <li><span style={{ color: EdgeColor("boolean") }}>{EdgeColor("boolean")}</span> - Boolean</li>
                            <li><span style={{ color: EdgeColor("string") }}>{EdgeColor("string")}</span> - String</li>
                            <li><span style={{ color: EdgeColor("minecraft:player") }}>{EdgeColor("minecraft:player")}</span> Minecraft - Player</li>
                            <li><span style={{ color: EdgeColor("discord:user") }}>{EdgeColor("discord:user")}</span> Discord - User</li>
                            <li><span style={{ color: EdgeColor("discord:channel") }}>{EdgeColor("discord:channel")}</span> Discord - Channel</li>
                        </ul>
                    </div>
                    <div>
                        <h1>Global Values</h1>
                        <button>Add Value</button>
                    </div>
                    <hr />
                    <div>
                        <h1>Commands</h1>
                        {Object.keys(configuration.commands).map((command) => {
                            return (
                                <div onClick={() => setSelectedCommand(command)} className={Style.command} key={command}>
                                    <h2>{command}</h2>
                                </div>
                            )
                        })}
                        <button onClick={() => add_commands()}>Add Command</button>
                    </div>

                    <hr />
                    <div>
                        <button onClick={() => {
                            document.getElementsByClassName(Style.exportedConf).item(0)?.classList.toggle(Style.open);
                        }}>Export</button>

                        <button onClick={() => {


                            const elem = (document.getElementById("impInput") as HTMLInputElement | undefined);
                            if (elem === undefined) return;
                            // Format it in human readable format
                            elem.value = JSON.stringify(configuration, null, 4);

                            document.getElementsByClassName(Style.importConf).item(0)?.classList.toggle(Style.open);
                        }}>Import</button>

                        <div className={Style.exportedConf}>
                            <div>
                                <h1>Exported Configuration</h1>

                                <pre className="language-javascript">
                                    <p className={Style.codeblock} ref={customCommandsConfDivRef}>
                                        <code>
                                            {JSON.stringify(configuration, null, 4)}
                                        </code>
                                    </p>
                                </pre>

                                <div>
                                    <button onClick={() => {
                                        const el = document.createElement('textarea');
                                        el.value = JSON.stringify(configuration, null, 4);
                                        document.body.appendChild(el);
                                        el.select();
                                        document.execCommand('copy');
                                        document.body.removeChild(el);
                                    }}>Copy</button>
                                    <button onClick={() => document.getElementsByClassName(Style.exportedConf).item(0)?.classList.toggle(Style.open)}>Close</button>
                                </div>
                            </div>
                        </div>


                        <div className={Style.importConf}>
                            <div>
                                <h1>Import Configuration</h1>

                                <pre className="language-javascript">
                                    <p className={Style.codeblock} ref={customCommandsConfDivRef}>
                                        <code>
                                            {/* {JSON.stringify(configuration, null, 4)} */}


                                            {/* <input id="impInput" type="text" defaultValue={JSON.stringify(configuration, null, 4)} /> */}
                                            {/* Same input but multiline */}
                                            <textarea id="impInput" defaultValue={JSON.stringify(configuration, null, 4)} />
                                        </code>
                                    </p>
                                </pre>

                                <div>
                                    <button onClick={() => {
                                        const elem = (document.getElementById("impInput") as HTMLInputElement | undefined);
                                        if (elem === undefined) return;
                                        try {
                                            const tmp = JSON.parse(elem.value);
                                            setConfiguration(tmp);
                                            alert("Imported!");
                                        } catch (e) {
                                            alert("Invalid JSON!");
                                        }
                                    }}>Import</button>
                                    <button onClick={() => document.getElementsByClassName(Style.importConf).item(0)?.classList.toggle(Style.open)}>Close</button>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
                <div className={Style.board}>
                    {configuration.commands[selectedCommand!!] === undefined ? (
                        <div className={Style.noSelected}>
                            <h1>No command selected</h1>
                            <p>Click the button bellow to create a new command!</p>
                            <button onClick={() => add_commands()}>Create a new command!</button>
                        </div>
                    ) : (
                        <svg cursor={isDraggingRef.current == true ? "grabbing" : "cursor"} ref={svgRef} preserveAspectRatio="none" onDrag={(event) => {
                            setBoardOffset([boardOffset[0] + event.movementX, boardOffset[1] + event.movementY]);
                            alert("Drag");
                        }}

                            onMouseDown={(event) => {
                                setSelectedNode(undefined);

                                // iff middle mouse button
                                if (event.button !== 1) return;
                                isDraggingRef.current = true;
                                startCoordsRef.current = { x: event.clientX, y: event.clientY };
                            }}

                            onMouseMove={(event) => {
                                if (isDraggingRef.current) {
                                    const deltaX = event.clientX - startCoordsRef.current.x;
                                    const deltaY = event.clientY - startCoordsRef.current.y;

                                    setBoardOffset((prevOffset) => ([
                                        prevOffset[0] + deltaX,
                                        prevOffset[1] + deltaY,
                                    ]));

                                    startCoordsRef.current = { x: event.clientX, y: event.clientY };
                                }
                            }}

                            onMouseUp={() => {
                                isDraggingRef.current = false;
                            }}

                            onWheel={(event) => {
                                // event.preventDefault();
                                setBoardScale(boardScale + event.deltaY * -0.001);
                            }}
                        >
                            <g>
                                {
                                    configuration.commands[selectedCommand!!].connections.map((connection, index) => {
                                        const sourceNode = configuration.commands[selectedCommand!!].nodes[connection.source.id];
                                        const targetNode = configuration.commands[selectedCommand!!].nodes[connection.target.id];
                                        if (sourceNode === undefined || targetNode === undefined) return null;
                                        // const sourceEdge = document.getElementById(`${sourceNode.id}_o${connection.source.output}`);
                                        // const targetEdge = document.getElementById(`${targetNode.id}_i${connection.target.input}`);
                                        // if (sourceEdge === null || targetEdge === null) return null;
                                        // const sourceEdgeRect = sourceEdge.getBoundingClientRect();
                                        // const targetEdgeRect = targetEdge.getBoundingClientRect();
                                        // const sourceEdgePos = [sourceEdgeRect.x + sourceEdgeRect.width / 2, sourceEdgeRect.y + sourceEdgeRect.height / 2];
                                        // const targetEdgePos = [targetEdgeRect.x + targetEdgeRect.width / 2, targetEdgeRect.y + targetEdgeRect.height / 2];



                                        // `translate(${node.position[0] * scale + boardOffset[0]}, ${node.position[1] * scale + boardOffset[1]})`

                                        const sourceEdgeLocalPos = GetEdgePosition(sourceNode, connection.source.output, "out", boardScale);
                                        const targetEdgeLocalPos = GetEdgePosition(targetNode, connection.target.input, "in", boardScale);

                                        const sourceEdgePos = [sourceNode.position[0] * boardScale + boardOffset[0] + sourceEdgeLocalPos[0] - 2 * boardScale, sourceNode.position[1] * boardScale + boardOffset[1] + sourceEdgeLocalPos[1] - 5 * boardScale]
                                        const targetEdgePos = [targetNode.position[0] * boardScale + boardOffset[0] + targetEdgeLocalPos[0] + 2 * boardScale, targetNode.position[1] * boardScale + boardOffset[1] + targetEdgeLocalPos[1] - 5 * boardScale]

                                        // bezier curve
                                        // draw circles at start and end (hollow if not connected)
                                        return (
                                            <g key={index}>
                                                selectedNode          <path onClick={ev => {
                                                    setSelectedNode(sourceNode);
                                                    setSelectedNodeEdge(connection.source.output);
                                                }}
                                                    key={index}
                                                    d={`M ${sourceEdgePos[0]} ${sourceEdgePos[1]} C ${sourceEdgePos[0] + 50} ${sourceEdgePos[1]} ${targetEdgePos[0] - 50} ${targetEdgePos[1]} ${targetEdgePos[0]} ${targetEdgePos[1]}`}
                                                    stroke={selectedNode?.id === sourceNode.id && selectedNodeEdge === connection.source.output ? "red" : "transparent"} strokeWidth="10"
                                                    strokeDasharray={getEdgeInputType(targetNode.type, connection.target.input) === "flow" ? "10,5" : ""}
                                                    cursor="pointer"
                                                    fill="transparent"
                                                    // Add background shadow to the line if the source node is selected
                                                    filter={selectedNode?.id === sourceNode.id && selectedNodeEdge === connection.source.output ? "drop-shadow(0 0 0.25rem rgba(0,0,0, 255))" : ""}

                                                />

                                                <path onClick={ev => {
                                                    setSelectedNode(sourceNode);
                                                    setSelectedNodeEdge(connection.source.output);
                                                }}
                                                    key={index}
                                                    d={`M ${sourceEdgePos[0]} ${sourceEdgePos[1]} C ${sourceEdgePos[0] + 50} ${sourceEdgePos[1]} ${targetEdgePos[0] - 50} ${targetEdgePos[1]} ${targetEdgePos[0]} ${targetEdgePos[1]}`}
                                                    stroke={"transparent"} strokeWidth="25"
                                                    cursor="pointer"
                                                    fill="transparent"

                                                />

                                                <path onClick={ev => {
                                                    setSelectedNode(sourceNode);
                                                    setSelectedNodeEdge(connection.source.output);
                                                }}
                                                    key={index}
                                                    d={`M ${sourceEdgePos[0]} ${sourceEdgePos[1]} C ${sourceEdgePos[0] + 50} ${sourceEdgePos[1]} ${targetEdgePos[0] - 50} ${targetEdgePos[1]} ${targetEdgePos[0]} ${targetEdgePos[1]}`}
                                                    stroke={EdgeColor(getEdgeInputType(targetNode.type, connection.target.input) ?? "flow")} strokeWidth="5"
                                                    strokeDasharray={getEdgeInputType(targetNode.type, connection.target.input) === "flow" ? "10,5" : ""}
                                                    cursor="pointer"

                                                    fill="transparent"
                                                    // Add background shadow to the line if the source node is selected
                                                    filter={selectedNode?.id === sourceNode.id && selectedNodeEdge === connection.source.output ? "drop-shadow(0 0 0.25rem rgba(0,0,0, 255))" : ""}

                                                    className={getEdgeInputType(targetNode.type, connection.target.input) === "flow" ? Style.stroke : ""}

                                                />


                                            </g>
                                        )
                                    }
                                    )
                                }
                                {tmpEdgeSelected === null ? null : (() => {
                                    const sourceNode = configuration.commands[selectedCommand!!].nodes[tmpEdgeSelected.source.id];
                                    const sourceEdge = document.getElementById(`${sourceNode.id}_o${tmpEdgeSelected.source.output}`);
                                    if (sourceEdge === null) return null;
                                    const sourceEdgeRect = sourceEdge.getBoundingClientRect();
                                    const sourceEdgePos = [sourceEdgeRect.x + sourceEdgeRect.width / 2, sourceEdgeRect.y + sourceEdgeRect.height / 2];
                                    // target pos is current mouse position
                                    const targetEdgePos = [mousePosition.x, mousePosition.y];

                                    // bezier curve
                                    return (
                                        <path style={{ pointerEvents: "none" }}
                                            d={`M ${sourceEdgePos[0]} ${sourceEdgePos[1]} C ${sourceEdgePos[0] + 50} ${sourceEdgePos[1]} ${targetEdgePos[0] - 50} ${targetEdgePos[1]} ${targetEdgePos[0]} ${targetEdgePos[1]}`}
                                            stroke="white" strokeWidth="5" fill="transparent"
                                            strokeDasharray="10,5"
                                        />
                                    )
                                })()}

                                {Object.entries(configuration.commands[selectedCommand!!].nodes).map(([name, node]) =>
                                (
                                    <g key={name}>

                                        <Node
                                            onNodeClick={ev => {
                                                ev.stopPropagation();
                                                setSelectedNode(node);
                                                setSelectedNodeEdge(undefined);
                                            }}
                                            key={name} node={node} scale={boardScale} boardOffset={boardOffset} connections={configuration.commands[selectedCommand!!].connections}
                                            setNode={(node: GraphNode) => {
                                                setConfiguration({
                                                    ...configuration, commands:
                                                    {
                                                        ...configuration.commands,
                                                        [selectedCommand!!]:
                                                        {
                                                            ...configuration.commands[selectedCommand!!],
                                                            nodes: {
                                                                ...configuration.commands[selectedCommand!!].nodes,
                                                                [name]: node
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                            }
                                            parentOffset={(svgRef.current as HTMLOrSVGElement) as HTMLElement}
                                            highlight={selectedNode?.id === node.id && selectedNodeEdge === undefined}
                                        />

                                    </g>
                                )
                                )}
                            </g>
                        </svg>
                    )
                    }
                </div>
                <div className={Style.nodes}>

                    {Array.from(nodeEdgeMap.entries()).map(([key, value]) => value.category).filter((value, index, self) => self.indexOf(value) === index).map((category) => (
                        <div className={`${Style.section} ${Style.open}`}>
                            <h1 onClick={(ev) => ev.currentTarget.parentElement?.classList.contains(Style.open) ? ev.currentTarget.parentElement?.classList.remove(Style.open) : ev.currentTarget.parentElement?.classList.add(Style.open)} >{category}</h1>
                            {Array.from(nodeEdgeMap.entries()).filter(([key, value]) => value.category === category).map(([key, value]) => (
                                <button onClick={() => add_node(key, 150, 150)} >{GetNodeName(key)}</button>
                            ))}
                        </div>
                    ))}

                </div>
            </div>
        </main>
    )
}