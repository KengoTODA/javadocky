'use strict';
const versionRepository = require('./version');

const verify = (str) => {
  // TODO artifactId and groupId must not contains '/' that enables directory traversal
  return str;
};

module.exports.badge = async (event, context, callback) => {
  const ext = verify(event.pathParameters.ext) || 'svg';
  const color = verify(event.pathParameters.color) || 'brightgreen';
  const groupId = verify(event.pathParameters.groupId);
  const artifactId = verify(event.pathParameters.artifactId);
  const version = await versionRepository.findLatest(groupId, artifactId);

  // redirect
  // https://aws.amazon.com/blogs/compute/redirection-in-a-serverless-api-with-aws-lambda-and-amazon-api-gateway/
  const err = new Error('Found');
  err.name = `https://img.shields.io/badge/javadoc-${version}-${color}.${ext}`

  callback(err, {});
};

module.exports.page = async (event, context, callback) => {
  const groupId = verify(event.pathParameters.groupId);
  const artifactId = verify(event.pathParameters.artifactId);
  const version = verify(event.pathParameters.version);
  // TODO verify for /../ or something like it
  const page = event.pathParameters.page || '';

  if (version === 'latest') {
    // load latest version from database, and use it
    version = await versionRepository.findLatest(groupId, artifactId);
  } else {
    // try to find version. If not found, we need to use given string as major version.
    const exists = await versionRepository.check(groupId, artifactId, version);
    if (!exists) {
      version = await versionRepository.findForMajor(groupId, artifactId, version);
    }
  }

  if (!version) {
    const response = {
      statusCode: 404,
      body: JSON.stringify({
        message: 'Not found',
        input: event,
      }),
    };
    callback(null, response);
  } else {
    // TODO redirect
    const response = {
      statusCode: 200,
      body: JSON.stringify({
        message: 'Go Serverless v1.0! Your function executed successfully!',
        input: event,
      }),
    };
    callback(null, response);
  }
};

module.exports.static = (event, context, callback) => {
  // redirect to the domain distributes static resource
};

module.exports.doc = (event, context, callback) => {
  // return the <frame> page
};
