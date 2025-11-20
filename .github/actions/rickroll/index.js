const core = require('@actions/core');
const github = require('@actions/github');

async function run() {
  try {
    // 1. Check probability (Standard logic)
    const percentage = parseInt(core.getInput('percentage'));
    if (Math.floor(Math.random() * 100) >= percentage) {
        console.log('Safe! No rickroll.');
        return;
    }

    const github_token = core.getInput('GITHUB_TOKEN');
    const context = github.context;
    const octokit = github.getOctokit(github_token);

    // 2. The Rickroll URL (YouTube or GIF)
    const rickrollUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

    // 3. Create a "Status Check" that links to the video
    // When the user sees the red "X" and clicks "Details", they get Rickrolled.
    await octokit.rest.repos.createCommitStatus({
        ...context.repo,
        sha: context.sha,
        state: 'failure',
        target_url: rickrollUrl, // <--- This redirects the "Details" click
        description: 'Critical Verification Failed. Click for logs.',
        context: 'continuous-integration/critical-logs' // Name of the check
    });

    console.log('Rickroll status check created. Waiting for victim to click "Details"...');

  } catch (error) {
    core.setFailed(error.message);
  }
}

run();
