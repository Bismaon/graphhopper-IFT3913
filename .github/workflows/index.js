const core = require('@actions/core');
const github = require('@actions/github');

async function run() {
  try {
    const github_token = core.getInput('GITHUB_TOKEN');
    // The Rickroll GIF
    const message = '![rickroll](https://user-images.githubusercontent.com/37572049/90699500-0cc3ec00-e2a1-11ea-8d13-989526e86b0e.gif)';

    const context = github.context;
    
    // Determine if this is an Issue or a Pull Request to get the number
    // github.context.issue.number handles both PRs and Issues automatically
    const issue_number = context.issue.number;

    if (!issue_number) {
        core.setFailed('No issue or pull request found to comment on.');
        return;
    }

    const octokit = github.getOctokit(github_token);
    
    await octokit.rest.issues.createComment({
        ...context.repo,
        issue_number: issue_number,
        body: message
      });

    console.log('Rickroll deployed successfully.');

  } catch (error) {
    core.setFailed(error.message);
  }
}

run();
