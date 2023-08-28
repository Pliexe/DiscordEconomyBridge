interface IProps {
    position: [number, number];
    text: string;
    direction: "in" | "out";
    connected: boolean;
    nodeId: string;
    edge: number;
    scale: number;
}

export default function ConnectableFlow(props: IProps) {
    return props.direction === "in" ?
        (
            <>
                {/* <text fontSize={`${props.scale}rem`} style={{ cursor: "pointer" }} id={`${props.nodeId}_i${props.edge}`} x={props.position[0] + 2} y={props.position[1]}></text> */}
                <text fontSize={`${props.scale}rem`} x={props.position[0] - 20 * props.scale} y={props.position[1]}>
                    <tspan cursor="pointer" id={`${props.nodeId}_i${props.edge}`}>{props.connected ? "▶" : "▷"}</tspan>
                    <tspan x="10px" cursor="pointer" id={`${props.nodeId}_i${props.edge}`}>➙</tspan>
                    {props.text}
                </text>
            </>
        ) : (
            <>
                {/* <text fontSize={`${props.scale}rem`} style={{ cursor: "pointer" }} id={`${props.nodeId}_o${props.edge}`} x={props.position[0] - 13} y={props.position[1]}>{props.connected ? "▶" : "▷"}</text> */}
                <text fontSize={`${props.scale}rem`} textAnchor="end" x={props.position[0] - 5} y={props.position[1]}>
                    {props.text}
                    <tspan cursor="pointer" id={`${props.nodeId}_o${props.edge}`}> {props.connected ? "▶" : "▷"}</tspan>
                </text>
            </>
        )
}