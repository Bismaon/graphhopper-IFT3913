const core = require('@actions/core');
const github = require('@actions/github');

async function run() {
  try {
    const percentage = parseInt(core.getInput('percentage'));
    const randomVal = Math.floor(Math.random() * 100);

    // If we roll higher than the percentage, we do NOT rickroll.
    // For this use case, you will pass '100', so this block is skipped.
    if (percentage <= randomVal) {
        console.log('Safe! No rickroll this time.');
        return;
    }

    const message = '![rickroll](https://user-images.githubusercontent.com/37572049/90699500-0cc3ec00-e2a1-11ea-8d13-989526e86b0e.gif)';
    console.log('Gottem!! Triggering Rickroll...');

    const github_token = core.getInput('GITHUB_TOKEN');
    const context = github.context;

    // LOGIC FIX: Determine if this is a PR or an Issue
    let issue_number;
    if (context.payload.pull_request) {
        issue_number = context.payload.pull_request.number;
    } else if (context.payload.issue) {
        issue_number = context.payload.issue.number;
    } else {
        console.log('No PR or Issue found in context. Skipping comment.');
        return;
    }

    const octokit = github.getOctokit(github_token);
    
    // API FIX: Use correct REST syntax
    await octokit.rest.issues.createComment({
        ...context.repo,
        issue_number: issue_number,
        body: message
    });

  } catch (error) {
    core.setFailed(error.message);
  }
}

run();
