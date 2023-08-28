import Style from "@/styles/components/node.module.scss";
import { Configuration } from "@/pages/visual-scripting";
import { FunctionComponent, KeyboardEvent, KeyboardEventHandler, MouseEvent, useState } from "react";
import { DraggableCore } from "react-draggable";
import { Connection, DataGraphNdoe, GetNodeName, GetTypeIcon, GraphNode, NodeType, nodeEdgeMap } from "@/interfaces/graph";
import ConnectableFlow from "./connectable_flow";
import ConnectableValue from "./connectable_value";
import EditString from "./edit_string";
import EditBoolan from "./edit_boolean";

interface IProps {
    key: string;
    node: GraphNode;
    setNode: (node: GraphNode) => void;
    connections: Connection[];
    parentOffset: HTMLElement;
    scale: number;
    boardOffset: [number, number];
    onKeyUp?: (ev: KeyboardEvent<SVGElement>) => void;
    onNodeClick?: (ev: MouseEvent<SVGElement>) => void;
    highlight?: boolean;
}

export const GetEdgePosition = (node: GraphNode, edge: number, direction: "in" | "out", scale: number): [number, number] => {
    if (direction == "in") return [-20 * scale, (50 + 30 * edge) * scale];
    else return [(getNodeSize(node.type)[0] + 20) * scale, (50 + 30 * edge) * scale];
}

const getNodeSize = (type: NodeType): [number, number] => {

    if (type == NodeType.DEFINE_BOOLEAN || type == NodeType.DEFINE_INT || type == NodeType.DEFINE_STRING || type == NodeType.DEFINE_FLOAT || type == NodeType.DEFINE_PLAYER || type == NodeType.DEFINE_DISCORD_USER) {

        if (type == NodeType.DEFINE_STRING) return [200, 100];

        return [200, 50 + 30 * Math.max(nodeEdgeMap.get(type)?.in.length ?? 0, nodeEdgeMap.get(type)?.out.length ?? 0)]
    }

    let txtSize =
        (nodeEdgeMap.get(type)?.in.reduce((prev, curr) => (curr.name?.length ?? 0) > prev.length ? curr.name!! : prev, "").length ?? 0) +
        (nodeEdgeMap.get(type)?.out.reduce((prev, curr) => (curr.name?.length ?? 0) > prev.length ? curr.name!! : prev, "").length ?? 0);


    const title = GetNodeName(type);

    txtSize = Math.max(txtSize, title.length);


    return [50 + (txtSize / (Math.log(txtSize)) * 30), 50 + 30 * Math.max(nodeEdgeMap.get(type)?.in.length ?? 0, nodeEdgeMap.get(type)?.out.length ?? 0)];
}

const IsDataGraphNode = (node: GraphNode): boolean => {
    switch (node.type) {
        case NodeType.DEFINE_BOOLEAN: return true;
        case NodeType.DEFINE_INT: return true;
        case NodeType.DEFINE_STRING: return true;
        case NodeType.DEFINE_FLOAT: return true;
        case NodeType.DEFINE_PLAYER: return true;
        case NodeType.DEFINE_DISCORD_USER: return true;
        default: return false;
    }
}

export const Node: FunctionComponent<IProps> = ({ key, node, parentOffset, boardOffset, scale, highlight, setNode, connections, onKeyUp, onNodeClick }) => {
    const [dragOffset, setDragOffset] = useState<[number, number]>([0, 0]);



    return (
        <>
            <DraggableCore key={key}
                onStart={(ev, data) => {
                    // get relative position
                    setDragOffset([
                        data.x - data.node.getBoundingClientRect().x * (1 / scale),
                        data.y - data.node.getBoundingClientRect().y * (1 / scale)
                    ]);
                }}
                onDrag={(ev, data) => {
                    setNode({
                        ...node,
                        position: [data.x - (boardOffset[0] * (1 / scale)) - dragOffset[0], data.y - boardOffset[1] * (1 / scale) - dragOffset[1]]
                    });
                }}
                // grid={boardSize}
                offsetParent={parentOffset}
                scale={scale}
            >
                <g key={key} x={node.position[0] * scale + boardOffset[0]} y={node.position[1] * scale + boardOffset[1]} transform={`translate(${node.position[0] * scale + boardOffset[0]}, ${node.position[1] * scale + boardOffset[1]})`}>
                    {/* <rect width={getNodeSize(node.type)[0] * scale} height={getNodeSize(node.type)[1] * scale} fill="red" stroke="white" strokeWidth="5" /> */}
                    {/* the same rect above but with shadow and rounded borders */}
                    <rect onMouseUp={onNodeClick} onKeyUp={onKeyUp} className={highlight ? `${Style.node} ${Style.highlight}` : Style.node} width={getNodeSize(node.type)[0] * scale} height={getNodeSize(node.type)[1] * scale} fill="rgba(0, 0, 0, 0.5)" stroke="white" strokeWidth="5" rx="10" ry="10" />

                    <text textAnchor="middle" fontWeight="bold" fontSize={`${scale}rem`} x={(getNodeSize(node.type)[0] * scale) / 2} y={20 * scale}>{GetNodeName(node.type)}</text>


                    {nodeEdgeMap.get(node.type)?.in.map((edge, i) => {
                        return <ConnectableValue scale={scale} nodeId={node.id} edge={i} position={GetEdgePosition(node, i, "in", scale)}
                            type={edge.type} text={edge.name ?? ""} direction="in" icon={GetTypeIcon(edge.type)}
                            connected={connections.some((connection) => connection.target.id == node.id && connection.target.input == i)}
                        />
                    }) ?? null}

                    {nodeEdgeMap.get(node.type)?.out.map((edge, i) => {

                        return <ConnectableValue
                            scale={scale} nodeId={node.id} edge={i} position={GetEdgePosition(node, i, "out", scale)}
                            direction="out" icon={GetTypeIcon(edge.type)}
                            connected={connections.some((connection) => connection.source.id == node.id && connection.source.output == i)}
                            type={edge.type} text={edge.name ?? ""}
                        />
                    }) ?? null}

                    {(() => {
                        switch (node.type) {
                            case NodeType.DEFINE_STRING:
                                return <EditString
                                    x={5} y={60 * scale}
                                    width={(getNodeSize(node.type)[0] - 10) * scale} height={30 * scale}
                                    value={(node as DataGraphNdoe<string>).data}
                                    placeholder="Enter a string"
                                    onChange={(value) => {
                                        setNode({
                                            ...node,
                                            // @ts-ignore
                                            data: value
                                        })
                                    }}
                                />;
                            case NodeType.DEFINE_BOOLEAN:
                                return <EditBoolan
                                    x={5} y={35 * scale}
                                    width={(getNodeSize(node.type)[0] - 10) * scale} height={20 * scale}
                                    value={(node as DataGraphNdoe<boolean>).data}
                                    onChange={(value) => {
                                        setNode({
                                            ...node,
                                            // @ts-ignore
                                            data: value
                                        })
                                    }}
                                />;
                        }
                    })()}
                    {/* <text textAnchor="middle" fontWeight="bold" fontSize={`${scale}rem`} x={(getNodeSize(node.type)[0] * scale) / 2} y={50 * scale}>TEST</text> */}


                    {/* {(() => {
                        switch (node.type) {
                            case NodeType.ON_COMMAND: return <OnCommand node={node} scale={scale} />;
                            case NodeType.ADD: return <Add node={node} scale={scale} />;
                            case NodeType.IF: return <If node={node} scale={scale} />;
                            case NodeType.GREATER:
                            case NodeType.LESS:
                            case NodeType.EQUAL:
                            case NodeType.GREATER_EQUAL:
                            case NodeType.LESS_EQUAL:
                            case NodeType.NOT_EQUAL: return <Comparison node={node} scale={scale} />;
                            case NodeType.IFGREATER:
                            case NodeType.IFLESS:
                            case NodeType.IFEQUAL:
                            case NodeType.IFGREATEREQUAL:
                            case NodeType.IFLESSEQUAL:
                            case NodeType.IFNOTEQUAL: return <IfComparison node={node} scale={scale} />;
                            case NodeType.OR: return <OrComparison node={node} scale={scale} />;
                            case NodeType.NOT: return <NotLogic node={node} scale={scale} />;
                            default: 
                            {
                                
                            }
                        }
                    })()} */}
                </g>

            </DraggableCore>



            {/* {node.next == null || nodes[node.next] == null ? null : (() => {
                const [x1, y1] = [node.location[0] * scale + 200, scale * node.location[1] + 45];
                const [x2, y2] = [nodes[node.next].location[0] * scale, scale * nodes[node.next].location[1] + 25];
                return (
                    // Dashed curved line
                    <path d={`M ${x1} ${y1} C ${x1 + 50} ${y1} ${x2 - 50} ${y2} ${x2} ${y2}`} stroke="white" strokeWidth="5" fill="transparent" strokeDasharray="10,5" />
                )
            })()} */}
        </>
    )
}