'use strict';

const AWS = require('aws-sdk');
const s3 = new AWS.S3();

/**
 * check that specified version exists for specified artifact
 * @return {Promise.<boolean>}
 */
module.exports.check = (groupId, artifactId, version) => {
  const key = `/${groupId}/${artifactId}/${version}/`;

  return new Promise((resolve, reject) => {
    var params = {
      Bucket: process.env.BUCKET,
      Delimiter: '/',
      Key: key
    };
    s3.headObject(params, function (err, data) {
      if (err) {
        if (err.code === 'NotFound') {
          resolve(false);
        } else {
          reject(err);
        }
      }
      resolve(true);
    });
  });
};
/**
 * list all available versions for specific artifact.
 * @return {Promise.<Array<string>>}
 */
module.exports.list = (groupId, artifactId) => {
  const prefix = `/${groupId}/${artifactId}/`;
  const prefixLength = prefix.length;

  return new Promise((resolve, reject) => {
    var params = {
      Bucket: process.env.BUCKET,
      Delimiter: '/',
      Prefix: prefix
    };
    s3.listObjectsV2(params, function (err, data) {
      if (err) reject(err);

      const keys = data.Contents.map((content) => content.Key.slice(prefixLength));
      resolve(keys);
    });
  });
};

/**
 * find latest version to
 * @return {Promise.<string>}
 */
module.exports.findLatest = (groupId, artifactId) => {
  const list = module.exports.list(groupId, artifactId);
  return list.then(list => list.sort())
            .then(sorted => sorted.length === 0 ? '' : sorted[0]);
};

/**
 * list all available versions for specific artifact.
 * @return {Promise.<string>}
 */
module.exports.findForMajor = (groupId, artifactId, majorVersion) => {
  const list = module.exports.list(groupId, artifactId);
  return list.then(list => list.filter(version => version.startsWith(majorVersion + '.')))
            .then(list => list.sort())
            .then(sorted => sorted.length === 0 ? '' : sorted[0]);
};
