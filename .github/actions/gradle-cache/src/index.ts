import {cp} from '@actions/io';
import {debug, getInput, warning} from '@actions/core';
import {context} from '@actions/github';
import {find, cacheDir} from '@actions/tool-cache';

const TOOL_NAME = 'gradle-cache';
async function main() {
  const mode = getInput('mode');
  if (mode == 'extract') {
    debug('extract mode');
    const cached = find(TOOL_NAME, '1.0.0');
    if (cached) {
      warning(`copy ${cached} to ${process.env.HOME}/.gradle/caches`)
      await cp(cached, `${process.env.HOME}/.gradle/caches`);
    } else {
      warning('no cache found for gradle');
    }
  } else {
    warning(`caching ${process.env.HOME}/.gradle/caches`)
    cacheDir(`${process.env.HOME}/.gradle/caches`, TOOL_NAME, '1.0.0');
  }
}
main();
