/**
* @type {import('next').NextConfig}
*/
const nextConfig = {
    images: {
      loader: 'akamai',
      path: '',
    },
    assetPrefix: process.env.NODE_ENV === 'production' ? '/DiscordEconomyBridge/' : undefined,
    basePath: process.env.NODE_ENV === 'production' ? '/DiscordEconomyBridge' : undefined,
    output: "export",
  };
  
  export default nextConfig;