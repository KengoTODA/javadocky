import * as core from '@actions/core';
import * as github from '@actions/github';

const token = core.getInput('token', { required: true });
const url = core.getInput('host_url');
const organization = core.getInput('organization');
const projectKey = core.getInput('project_key');

interface IParam {
  [key: string]: string
}

const params: IParam = {
  'sonar.login': token,
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
  params['sonar.branch.name'] = github.context.ref.substr('refs/heads/'.length);
} else if (github.context.eventName == 'pull_request') {
  params['sonar.pullrequest.provider'] = 'GitHub';

  const pr = github.context.payload.pull_request;
  if (pr) {
    params['sonar.pullrequest.key'] = '' + pr.number;
    params['sonar.pullrequest.branch'] = pr.head.ref;
    params['sonar.pullrequest.base'] = pr.base.ref;
  }
}
core.exportVariable('SONARQUBE_SCANNER_PARAMS', JSON.stringify(params));
