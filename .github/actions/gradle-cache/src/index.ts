import {cp} from '@actions/io';
import {debug, getInput, warning} from '@actions/core';
import {context} from '@actions/github';
import {find, cacheDir} from '@actions/tool-cache';

async function main() {
  const mode = getInput('mode');
  if (mode == 'extract') {
    debug('extract mode');
    const cached = find('gradle/caches', '1.0.0');
    if (cached) {
      warning(`copy ${cached} to ${process.env.HOME}/.gradle/caches`)
      await cp(cached, `${process.env.HOME}/.gradle/caches`);
    }
  } else {
    debug('cache mode');
    warning(`caching ${process.env.HOME}/.gradle/caches`)
    cacheDir(`${process.env.HOME}/.gradle/caches`, 'gradle/caches', '1.0.0');
  }
}
main();
