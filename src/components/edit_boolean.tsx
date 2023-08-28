import Style from '../styles/components/edit_boolean.module.scss'

interface IProps {
    x: number;
    y: number;
    width: number;
    height: number;

    value?: boolean;
    onChange?: (value: boolean) => void;
}

export default function EditBoolan(props: IProps) {
    return (

        <foreignObject x={props.x} y={props.y} width={props.width} height={props.height}>

            <div className={Style.bool}>
                <div>
                    <label>True</label>
                    <input onClick={() => { if(props.onChange) props.onChange(true)} } type="radio" name="True" id="" checked={props.value ?? false} />
                </div>
                <div>
                    <label>False</label>
                    <input onClick={() => { if(props.onChange) props.onChange(false)} } type="radio" name="False" id="" checked={props.value == undefined ? false : !props.value} />
                </div>
            </div>

        </foreignObject>
    )
}