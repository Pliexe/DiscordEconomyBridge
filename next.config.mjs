/**
* @type {import('next').NextConfig}
*/
const nextConfig = {
    images: {
      loader: 'akamai',
      path: '',
    },
    assetPrefix: '/DiscordEconomyBridge/',
    basePath: '/DiscordEconomyBridge',
    output: "export",
  };
  
  export default nextConfig;