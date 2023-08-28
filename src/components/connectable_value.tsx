import Style from '@/styles/components/connectable_value.module.scss';
import { EdgeColor, NodeEdgeType } from "@/interfaces/graph";

interface IProps {
    position: [number, number];
    type: NodeEdgeType;
    text: string;
    direction: "in" | "out";
    connected: boolean;
    nodeId: string;
    edge: number;
    scale: number;
    icon?: string;
}



export default function ConnectableValue(props: IProps) {
    // console.log("ConnectableValue: " + `${props.nodeId}_${props.direction == "out" ? "o" : "i"}${props.edge}`); // TODO: Fix this
    // console.log(props);

    return props.direction === "in" ?
        (
            <>
                {/* <text fontSize={`${props.scale}rem`} className={Style.circle} id={`${props.nodeId}_i${props.edge}`} style={{ fill: EdgeColor(props.type) }} x={props.position[0]} y={props.position[1]}>{props.connected ? "⚫" : "⚪"}</text> */}
                <text fontSize={`${props.scale}rem`} x={props.position[0]} y={props.position[1]} fill="white">
                    <tspan cursor="pointer" id={`${props.nodeId}_i${props.edge}`} fill={EdgeColor(props.type)}>{props.type == "flow" ? (props.connected ? "▶" : "▷") : (props.connected ? "⚫" : "⚪")}</tspan>&nbsp;&nbsp;
                    <tspan cursor="pointer" fill={EdgeColor(props.type)}>{props.icon ?? "⚫"} </tspan>
                    {props.text}
                </text>
            </>
        ) : (
            <>
                {/* <text fontSize={`${props.scale}rem`} className={Style.circle} id={`${props.nodeId}_o${props.edge}`} textAnchor="end" style={{ fill: EdgeColor(props.type) }} x={props.position[0]} y={props.position[1]} fill={EdgeColor(props.type)}>{props.connected ? "⚫" : "⚪"}</text> */}
                <text fontSize={`${props.scale}rem`} textAnchor="end" x={props.position[0]} y={props.position[1]} fill="white">
                    {props.text}
                    {/* <tspan cursor="pointer" id={`${props.nodeId}_o${props.edge}`} fill={EdgeColor(props.type)}> {props.connected ? "⚫" : "⚪"}</tspan> */}
                    &nbsp;
                    <tspan cursor="pointer" fill={EdgeColor(props.type)}>{props.icon ?? "⚫"}</tspan>&nbsp;&nbsp;
                    <tspan cursor="pointer" id={`${props.nodeId}_o${props.edge}`} fill={EdgeColor(props.type)}>{props.connected ? "⚫" : "⚪"}</tspan>
                    
                </text>
            </>
        )
}