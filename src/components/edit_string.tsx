import Style from '../styles/components/edit_string.module.scss'

interface IProps {
    x: number;
    y: number;
    width: number;
    height: number;

    placeholder?: string;
    value?: string;
    onChange?: (value: string) => void;
}

export default function EditString(props: IProps) {
    return (

        <foreignObject x={props.x} y={props.y} width={props.width} height={props.height}>

            <input className={Style.input} type="text" placeholder={props.placeholder} value={props.value} onChange={(ev) => props.onChange?.(ev.target.value)} />

        </foreignObject>
    )
}