"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const io_1 = require("@actions/io");
const core_1 = require("@actions/core");
const tool_cache_1 = require("@actions/tool-cache");
const TOOL_NAME = 'gradle-cache';
function main() {
    return __awaiter(this, void 0, void 0, function* () {
        const mode = core_1.getInput('mode');
        if (mode == 'extract') {
            core_1.debug('extract mode');
            const cached = tool_cache_1.find(TOOL_NAME, '1.0.0');
            if (cached) {
                core_1.warning(`copy ${cached} to ${process.env.HOME}/.gradle/caches`);
                yield io_1.cp(cached, `${process.env.HOME}/.gradle/caches`);
            }
            else {
                core_1.warning('no cache found for gradle');
            }
        }
        else {
            core_1.warning(`caching ${process.env.HOME}/.gradle/caches`);
            tool_cache_1.cacheDir(`${process.env.HOME}/.gradle/caches`, TOOL_NAME, '1.0.0');
        }
    });
}
main();
