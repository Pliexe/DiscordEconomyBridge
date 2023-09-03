import Link from 'next/link';
import Style from '../styles/pages/index.module.scss'
import { useState } from "react";

export default function Home() {
    const [clicks, setClicks] = useState(0);

    return (
        <main>
            <div className={Style.test} style={{ flexDirection: "column", display: "flex", height: "100vh", width: "100vw", justifyContent: "center", alignItems: "center" }}>
                <h1 style={{ fontWeight: "bold", fontSize: "5rem" }}>Work in progress! ;)</h1>
                {/* <button style={{ color: "white" }} onClick={(ev) => setClicks(clicks + 1)}>Click me! {clicks}</button> */}
                <p>Click the button below to checkout the experimental visual scripting!</p>
                <Link href="/visual-scripting">
                    <button style={{ color: "white" }}>Checkout Experimental Visual Scripting!</button>
                </Link>
            </div>
        </main>
    )
}