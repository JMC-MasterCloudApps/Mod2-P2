import AWS from 'aws-sdk';

// Create table (if not exist)

const createTableIfNotExist = async (tableName) => {

    // Create client at function level to have right config
    const dynamobDB = new AWS.DynamoDB();

    return dynamobDB.createTable({ 
        TableName: tableName, 
        AttributeDefinitions: [
            {
                AttributeName: 'id',
                AttributeType: 'N'
            },
        ],
        KeySchema: [ 
            {
                AttributeName: 'id',
                KeyType: 'HASH'
            },
        ],
        ProvisionedThroughput: {
            ReadCapacityUnits: 5,
            WriteCapacityUnits: 5
        },
    }).promise().catch((reason)=>{
        if(reason.message != "Cannot create preexisting table"){
            console.error(reason.message)
        }
    })
}

export default createTableIfNotExist;
