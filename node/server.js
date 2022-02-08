import app from './src/app.js';
import AWS from 'aws-sdk';
const port = process.env.PORT || 3000;
import createTableIfNotExist from './src/db/createTable.js'

// CONFIGURE AWS TO USE LOCAL REGION AND DEFAULT ENDPOINT (LOCALHOST) FOR DYNAMODB
AWS.config.update({
  region: process.env.AWS_REGION || 'local',
  endpoint: process.env.AWS_DYNAMO_ENDPOINT || 'http://localhost:8000',
  accessKeyId: "xxxxxx", // No es necesario poner nada aquí
  secretAccessKey: "xxxxxx" // No es necesario poner nada aquí
});

// CREATE DYNAMODB TABLE ONLY IF NOT EXIST PREVIOUSLY
(async() => { 
  await createTableIfNotExist("films");
})();

app.listen(port, () => {
  console.log('Express server listening on port ' + port);
});
