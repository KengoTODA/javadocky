import {cp} from '@actions/io';
import {debug, getInput} from '@actions/core';
import {context} from '@actions/github';
import {find, cacheDir} from '@actions/tool-cache';

async function main() {
  const mode = getInput('mode');
  if (mode == 'extract') {
    const cached = find('gradle dir', '1.0.0');
    if (cached) {
      debug('extracting...')
      await cp(cached, `${process.env.HOME}/.gradle`);
    }
  } else {
    debug('caching...gst')
    cacheDir(`${process.env.HOME}/.gradle`, 'gradle dir', '1.0.0');
  }
}
main();
