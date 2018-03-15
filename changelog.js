#!/usr/bin/env node

const git = require('simple-git');
const changelog = require('generate-changelog');
const fs = require('fs');
const idx = require('idx');
const argv = require('minimist')(process.argv.slice(1));

git().tags((err, tags) => {
  const currentChangelog = fs.readFileSync('./CHANGELOG.md');
  const matched = tags.latest.match(/v\d+.\d+.\d+-(\d+)/);
  const build = (idx(matched, _ => Number(_[1])) || 0) + 1;

  changelog
    .generate({
      major: argv.major,
      minor: argv.minor,
      patch: argv.patch,
    })
    .then(function(changelog) {
      const rxVersion = /\d+\.\d+\.\d+/;
      const newVersion = argv.version || idx(changelog.match(rxVersion), _ => _[0]) + `-${build}`;

      changelog = changelog.replace(rxVersion, newVersion) + currentChangelog;
      fs.writeFileSync('./CHANGELOG.md', changelog);

      const addFile = c => git().add('CHANGELOG.md', c);
      const commit = c => git().commit(`build(change-log): v${newVersion}`, c);
      const addTag = c => git().addAnnotatedTag(`v${newVersion}`, `build(tag): v${newVersion}`, c);

      addFile(() => commit(() => addTag()));
    });
});
