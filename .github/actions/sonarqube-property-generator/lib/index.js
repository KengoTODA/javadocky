"use strict";
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
    result["default"] = mod;
    return result;
};
Object.defineProperty(exports, "__esModule", { value: true });
const core = __importStar(require("@actions/core"));
const github = __importStar(require("@actions/github"));
const token = core.getInput('token', { required: true });
const url = core.getInput('host_url');
const organization = core.getInput('organization');
const projectKey = core.getInput('project_key');
const params = {
    'sonar.login': token
};
if (url) {
    params['sonar.host.url'] = url;
}
if (organization) {
    params['sonar.organization'] = organization;
}
if (projectKey) {
    params['sonar.projectKey'] = projectKey;
}
if (github.context.eventName == 'push' && github.context.ref != 'refs/heads/master') {
    params['sonar.branch.name'] = github.context.ref;
}
else if (github.context.eventName == 'pull_request') {
    params['sonar.pullrequest.key'] = github.context.payload.id;
    params['sonar.pullrequest.branch'] = github.context.payload.head.ref;
    params['sonar.pullrequest.base'] = github.context.payload.base.ref;
}
core.exportVariable('SONARQUBE_SCANNER_PARAMS', JSON.stringify(params));
