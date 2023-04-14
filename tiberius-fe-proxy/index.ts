// include dependencies
require('dotenv').config({
  path: `.env.${process.env.NODE_ENV}`
})
import express from 'express';
import cors from 'cors';
import morgan from 'morgan';
import { createProxyMiddleware, Options } from 'http-proxy-middleware';
import prompt from 'prompt';
import { cc } from './consoleColor';

const init = async () => {
  console.log(`
    ${cc.dim('Get x-augustus-token:')} ${cc.cyan(`${process.env.AUGUSTUS_HOST}/api/user/token`)}
  `)

  prompt.start();
  const { tcad, xAugustusToken } = await prompt.get(['tcad', 'xAugustusToken']);

  // proxy middleware options
  const options: Options = {
    target: process.env.K8S_HOST,
    changeOrigin: true,
    followRedirects: true,
    pathRewrite: {
      '^/api/tiberius/config/env/local': process.env.K8S_TIBERIUS_CONFIG_BASE_PATH,
      '^/api/tiberius': process.env.K8S_TIBERIUS_BASE_PATH,
      '^/api/springfield': '/'
    },
    secure: false,
    onProxyRes: (proxyRes) => {
      proxyRes.headers['Access-Control-Allow-Origin'] = '*';
    },
    onProxyReq: (proxyReq) => {
      proxyReq.setHeader('x-augustus-token', xAugustusToken);
      proxyReq.setHeader('x-tcad', tcad);
    },
    logLevel: 'debug'
  };

  // Do not use middleware for session endpoints, they are mocked here since I cant figure out how to request to gateway...
  const filter = function (pathname: string) {
    return !pathname.match('^/api/session');
  };

  const exampleProxy = createProxyMiddleware(filter, options);
  const app = express()

  app.use(cors())
  app.use(morgan('dev'))
  app.use('*', exampleProxy);

  app.get('/api/session/init', (_, res) => {
    res.status(200)
    res.send({})
  })

  app.get('/api/session/expiration', (_, res) => {
    res.status(200)
    res.send({})
  })

  const server = app.listen(8082);

  console.log(`
┌───────────────────────────┤ ${cc.green('tiberius-fe-proxy')} ├───────────────────────────────┐
  ${cc.dim('Server:')} http://localhost:${server.address().port}
  ${cc.dim('Environment:')} ${cc.red(String(process.env.NODE_ENV).toUpperCase())}
  ${cc.dim('tcad:')} ${tcad}
  ${cc.dim('x-augustus-token:')} ${xAugustusToken.substring(0, 50)}...
└───────────────────────────────────────────────────────────────────────────────┘
  `)
}

init()
