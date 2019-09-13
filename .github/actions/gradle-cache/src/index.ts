import {cp} from '@actions/io';
import {getInput} from '@actions/core';
import {context} from '@actions/github';
import {find, cacheDir} from '@actions/tool-cache';

async function main() {
  const mode = getInput('mode');
  if (mode == 'extract') {
    const cached = find('gradle dir', '1.0.0');
    if (cached) {
      await cp(cached, `${process.env.HOME}/.gradle`);
    }
  } else {
    cacheDir(`${process.env.HOME}/.gradle`, 'gradle dir', '1.0.0');
  }
}
main();
