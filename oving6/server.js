const http = require('http');
const crypto = require('crypto');

const port = 3000;
let sockets = [];

const server = http.createServer((req, res) => {
    let content = `<!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8" />
        </head>
        <body>
            WebSocket test page
            <br/>
            <label for="lname">Skriv melding:</label>
            <input type="text" id="message" name="message"><br><br>
            <input type="submit" value="Submit" onclick="return onClickSubmit();">
            
            <div id="chat" class="div">
            </div>
            <style>
            .div {
                border: 1px solid black;
                width: 500px;
                height: auto;
                margin-top: 10px;
            }
            </style>
            <script>
                let ws = new WebSocket('ws://localhost:3000', ["json"]);
                ws.addEventListener("open", () => {
                    console.log("Websocket connection opened");
                    ws.send(JSON.stringify({message: 'Hello!'}));
                });
                ws.addEventListener('message', event => {
                    const data = JSON.parse(event.data);
                    console.log('Received:', data);
                    document.getElementById("chat").innerHTML = document.getElementById("chat").innerHTML + "</br> Friend: "+ data.message;
                });
                const onClickSubmit = () => {
                    let v = document.getElementById("message").value
                    ws.send(JSON.stringify({message: v}));
                    document.getElementById("chat").innerHTML = document.getElementById("chat").innerHTML + "</br> You: "+ v;
                }
            </script>
        </body>
        </html>
        `;
    res.writeHead(200, {'Content-Type': 'text/html'});
    res.write(content);
    res.end();
}).listen(port, () => console.log(`HTTP server listening on port ${port}`));

server.on('upgrade', (req, socket) => {
    // Make sure that we only handle WebSocket upgrade requests
    if (req.headers['upgrade'] !== 'websocket') {
      socket.end('HTTP/1.1 400 Bad Request');
      return;
    }

    const acceptKey = req.headers['sec-websocket-key']; 
    const hash = generateAcceptValue(acceptKey); 
    const responseHeaders = [
        "HTTP/1.1 101 Web Socket Protocol Handshake",
        "Upgrade: WebSocket",
        "Connection: Upgrade",
        `Sec-WebSocket-Accept: ${hash}`,
    ]; 

    const protocol = req.headers['sec-websocket-protocol'];
    const protocols = protocol ? protocol.split(",").map((s) => s.trim()) : [];
    
    if (protocols.includes('json')) 
        responseHeaders.push(`Sec-WebSocket-Protocol: json`);

    socket.write(responseHeaders.join("\r\n") + "\r\n\r\n");

    sockets.push(socket);

    socket.on('data', buffer => {
        console.log('Data received from client: ', buffer.toString());
        const message = parseMessage(buffer);

        if (message)          
            sockets.forEach(socket => {
                socket.write(constructReply({ message: message.message })); 
            });
        else if (message === null) 
            console.log('WebSocket connection closed by the client.'); 
    });

    socket.on('error', (error) => {
        console.error('Error: ', error);
    });
});

server.on('error', (error) => {
    console.error('Error: ', error);
});

  const parseMessage = (buffer) => {
    const firstByte = buffer.readUInt8(0);
    const opCode = firstByte & 0xf;
    if (opCode === 0x8) 
      return null;
      
    if (opCode !== 0x1) 
      return;

    const secondByte = buffer.readUInt8(1);
    const isMasked = Boolean((secondByte >>> 7) & 0x1);
  
    let currentOffset = 2;
    let payloadLength = secondByte & 0x7f;
    if (payloadLength > 125) {
      if (payloadLength === 126) {
        payloadLength = buffer.readUInt16BE(currentOffset);
        currentOffset += 2;
      } else {
        throw new Error("Large payloads are not supported");
      }
    }
  
    let maskingKey;
    if (isMasked) {
      maskingKey = buffer.readUInt32BE(currentOffset);
      currentOffset += 4;
    }

    const data = Buffer.alloc(payloadLength);
    if (isMasked) {
      for (let i = 0, j = 0; i < payloadLength; ++i, j = i % 4) {
        const shift = j == 3 ? 0 : (3 - j) << 3;
        const mask = (shift == 0 ? maskingKey : maskingKey >>> shift) & 0xff;
        const source = buffer.readUInt8(currentOffset++);
        data.writeUInt8(mask ^ source, i);
      }
    } else {
      buffer.copy(data, 0, currentOffset++);
    }

    const json = data.toString("utf8");
    return JSON.parse(json);
  };

function constructReply (data) {
    // Convert the data to JSON and copy it into a buffer
    const json = JSON.stringify(data)
    const jsonByteLength = Buffer.byteLength(json);
    // Note: we're not supporting > 65535 byte payloads at this stage 
    const lengthByteCount = jsonByteLength < 126 ? 0 : 2; 
    const payloadLength = lengthByteCount === 0 ? jsonByteLength : 126; 
    const buffer = Buffer.alloc(2 + lengthByteCount + jsonByteLength); 
    // Write out the first byte, using opcode `1` to indicate that the message 
    // payload contains text data 
    buffer.writeUInt8(0b10000001, 0); 
    buffer.writeUInt8(payloadLength, 1); 
    // Write the length of the JSON payload to the second byte 
    let payloadOffset = 2; 
    if (lengthByteCount > 0) {
      buffer.writeUInt16BE(jsonByteLength, 2); 
      payloadOffset += lengthByteCount; 
    }

    // Write the JSON data to the data buffer 
    buffer.write(json, payloadOffset); 
    return buffer;
  }

function generateAcceptValue (acceptKey) {
  return crypto
  .createHash('sha1')
  .update(acceptKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11", 'binary')
  .digest('base64');
}