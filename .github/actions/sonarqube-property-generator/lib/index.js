"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const core_1 = __importDefault(require("@actions/core"));
const github_1 = __importDefault(require("@actions/github"));
const token = core_1.default.getInput('token', { required: true });
const url = core_1.default.getInput('host_url');
const organization = core_1.default.getInput('organization');
const projectKey = core_1.default.getInput('project_key');
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
if (github_1.default.context.eventName == 'push' && github_1.default.context.ref != 'refs/heads/master') {
    params['sonar.branch.name'] = github_1.default.context.ref;
}
else if (github_1.default.context.eventName == 'pull_request') {
    params['sonar.pullrequest.key'] = github_1.default.context.payload.id;
    params['sonar.pullrequest.branch'] = github_1.default.context.payload.head.ref;
    params['sonar.pullrequest.base'] = github_1.default.context.payload.base.ref;
}
core_1.default.exportVariable('SONARQUBE_SCANNER_PARAMS', JSON.stringify(params));
