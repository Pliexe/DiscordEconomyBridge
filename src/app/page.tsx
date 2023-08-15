import Image from 'next/image'
"use client"
import { useState } from 'react'

export default function Home() {
  const [clicks, setClicks] = useState(0);
  
  return (
    <main>
      <div style={{ flexDirection: "column", display: "flex", height: "100vh", width: "100vw", justifyContent: "center", alignItems: "center" }}>
        <h1 style={{ fontWeight: "bold", fontSize: "5rem" }}>Work in progress! ;)</h1>
        <button style={{ color: "white" }} onClick={(ev) =>  setClicks(clicks+1)}>Click me! {clicks}</button>
      </div>
    </main>
  )
}
