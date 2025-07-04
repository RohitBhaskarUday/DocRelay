/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  swcMinify: true,
  async rewrites() {
    return [
      {
        source: '/api/upload',
        destination: 'http://localhost:9000/upload',
      },
      {
        source: '/api/download/:port',
        destination: 'http://localhost:9000/download/:port',
      },
    ];
  },
}

module.exports = nextConfig
