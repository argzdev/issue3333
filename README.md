# issue3333
TLDR: Adding self service SSL for APIs causes conflict with the SSL of Firebase Remote Config.

### Prerequisite
- Enable Remote Config, add a string `random_value` with any value
- NodeJS server
```
const express = require('express')
const https = require('https')
const path = require('path')
const fs = require('fs')
const app = express()

app.use('/', (req, res, next) => {
    res.send('Hello from SSL server')
})

const sslServer = https.createServer({
    key: fs.readFileSync(path.join(__dirname, 'certs', '{key_file_name}.pem')),
    cert: fs.readFileSync(path.join(__dirname, 'certs', '{cert_file_name}.pem')),
}, app)

sslServer.listen(3443, () => console.log('Secure server on port 3443'))
```
- Add google-services.json in Android app
- Add cert.crt in raw folder in Android app

   1. To create cert.crt, you need to use `mkcert` in https://github.com/FiloSottile/mkcert. 
   2. Use cmd and type `mkcert localhost 10.0.2.2`. This will create 2 files, key.pem & cert.pem. 
   3. Add these 2 files into your NodeJS server, replace {key_file_name} & {cert_file_name}
   4. Create a cert.crt file in raw folder of the Android app, replace the content of the cert.crt file with the content of the cert.pem.

### Summary
- When running the app, the error `javax.net.ssl.SSLHandshakeException: java.security.cert.CertPathValidatorException: Trust anchor for certification path not found.` is encountered.
- If remote config is called before the SSL is set, the issue is not encountered. This seems like a race condition.
- Not using the `allowSSL()` method will allow the app to work properly.
