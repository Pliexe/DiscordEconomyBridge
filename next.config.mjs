/**
* @type {import('next').NextConfig}
*/
const nextConfig = {
    images: {
      loader: 'akamai',
      path: '',
    },
    assetPrefix: 'https://github.com/Pliexe/DiscordEconomyBridge/tree/public',
    output: "export",
  };
  
  export default nextConfig;