/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

// See https://docusaurus.io/docs/site-config for all the possible
// site configuration options.

// List of projects/orgs using your project for the users page.
const users = [
  // example to add showcase
  // {
  //   caption: 'User1',
  //   // You will need to prepend the image path with your baseUrl
  //   // if it is not '/', like: '/test-site/img/image.jpg'.
  //   image: '/img/undraw_open_source.svg',
  //   infoLink: 'https://www.facebook.com',
  //   pinned: true,
  // },
];

const siteConfig = {
  title: 'React Native Camera', // Title for your website.
  tagline: 'The comprehensive camera module for React Native.',

  //url: 'https://react-native-community/github.io', // Your website URL
  baseUrl: '/react-native-camera/',
  projectName: 'react-native-camera',
  organizationName: 'react-native-community',

  headerLinks: [
    { doc: 'installation', label: 'Docs' },
    { doc: 'expo_usage', label: 'Guides' },
    { doc: 'api', label: 'API' },
    { page: 'docs/qa', label: 'Help' },
    { doc: 'Tidelift', label: 'For Enterprise' },
    {
      href: 'https://github.com/react-native-community/react-native-camera',
      label: 'GitHub',
    },
  ],

  // If you have users set above, you add it here:
  users,

  /* path to images for header/footer */
  headerIcon: 'img/community.png',
  footerIcon: 'img/community.png',
  favicon: 'img/community.png',

  /* Colors for website */
  colors: {
    primaryColor: '#111928',
    secondaryColor: '#D73964',
  },

  /* Custom fonts for website */
  /*
  fonts: {
    myFont: [
      "Times New Roman",
      "Serif"
    ],
    myOtherFont: [
      "-apple-system",
      "system-ui"
    ]
  },
  */

  // This copyright info is used in /core/Footer.js and blog RSS/Atom feeds.
  copyright: `Copyright © ${new Date().getFullYear()}`,
  usePrism: ['jsx'],
  highlight: {
    theme: 'atom-one-dark',
  },
  scripts: [
    'https://buttons.github.io/buttons.js',
    'https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.0/clipboard.min.js',
    '/js/code-blocks-buttons.js',
  ],
  stylesheets: ['/css/code-block-buttons.css'],
  // On page navigation for the current documentation page.
  onPageNav: 'separate',
  // No .html extensions for paths.
  cleanUrl: true,

  // Open Graph and Twitter card images.
  ogImage: 'img/undraw_online.svg',
  twitterImage: 'img/undraw_tweetstorm.svg',

  // Show documentation's last contributor's name.
  enableUpdateBy: true,

  // Show documentation's last update time.
  enableUpdateTime: true,
  editUrl: 'https://github.com/react-native-community/react-native-camera/master/docs/',

  // You may provide arbitrary config keys to be used as needed by your
  // template. For example, if you need your repo's URL...
  repoUrl: 'https://github.com/react-native-community/react-native-camera',
};

module.exports = siteConfig;
