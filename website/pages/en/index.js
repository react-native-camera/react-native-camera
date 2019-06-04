/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

const CompLibrary = require('../../core/CompLibrary.js');

const Container = CompLibrary.Container;
const GridBlock = CompLibrary.GridBlock;

class HomeSplash extends React.Component {
  render() {
    const {siteConfig, language = ''} = this.props;
    const {baseUrl, docsUrl} = siteConfig;
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
    const langPart = `${language ? `${language}/` : ''}`;
    const docUrl = doc => `${baseUrl}${docsPart}${langPart}${doc}`;

    const SplashContainer = props => (
      <div className="homeContainer">
        <div className="homeSplashFade">
          <div className="wrapper homeWrapper">{props.children}</div>
        </div>
      </div>
    );

    const Logo = props => (
      <div className="projectLogo">
        <img src={props.img_src} alt="Project Logo" />
      </div>
    );

    const ProjectTitle = () => (
      <h2 className="projectTitle">
        {siteConfig.title}
        <small>{siteConfig.tagline}</small>
      </h2>
    );

    const PromoSection = props => (
      <div className="section promoSection">
        <div className="promoRow">
          <div className="pluginRowBlock">{props.children}</div>
        </div>
      </div>
    );

    const Button = props => (
      <div className="pluginWrapper buttonWrapper">
        <a className="button" href={props.href} target={props.target}>
          {props.children}
        </a>
      </div>
    );

    return (
      <SplashContainer>
        <Logo img_src={`${baseUrl}img/undraw_monitor.svg`} />
        <div className="inner">
          <ProjectTitle siteConfig={siteConfig} />
          <PromoSection>
            <Button href={docUrl('installation.html')}>Try It Out</Button>
            <Button href="#support">Support</Button>
            <Button href="#collective">Open Collective</Button>
          </PromoSection>
        </div>
      </SplashContainer>
    );
  }
}

class Index extends React.Component {
  render() {
    const { config: siteConfig, language = '' } = this.props;
    const { baseUrl } = siteConfig;

    const Block = props => (
      <Container
        padding={['bottom', 'top']}
        id={props.id}
        background={props.background}>
        <GridBlock
          align="center"
          contents={props.children}
          layout={props.layout}
        />
      </Container>
    );

    const Support = () => (
      <Block id="support">
        {[
          {
            content:
              'To make your landing page more attractive, use illustrations! Check out ' +
              'If you use this library on your commercial/personal projects, you can help us by funding the work on specific issues that you choose by using [**IssueHunt.io**](https://issuehunt.io/repos/33218414)! \n\n' +
              'This gives you the power to prioritize our work and support the project contributors. Moreover it\'ll guarantee the project will be updated and maintained in the long run.' +
              '\n\n[![issuehunt-image](https://issuehunt.io/static/embed/issuehunt-button-v1.svg)](https://issuehunt.io/repos/33218414)',
            image: `${baseUrl}img/undraw_code_review.svg`,
            imageAlign: 'right',
            title: 'Support',
          },
        ]}
      </Block>
    );

    const backersList = Array(10).fill('x').map( (bk, i) => `<a href="https://opencollective.com/react-native-camera/backer/${i}/website" target="_blank"><img src="https://opencollective.com/react-native-camera/backer/${i}/avatar.svg"></a>`)
    const sponsorsList = Array(10).fill('x').map( (bk, i) => `<a href="https://opencollective.com/react-native-camera/sponsor/${i}/website" target="_blank"><img src="https://opencollective.com/react-native-camera/sponsor/${i}/avatar.svg"></a>`)

    const Backers = () => (
      <Block id="collective" background="light">
        {[
          {
            content: `**Backers** 
            \n\nSupport us with a monthly donation and help us continue our activities. [[**Become a backer**](https://opencollective.com/react-native-camera#backer)] \n\n\n ${backersList.join("")}
            \n\n\n**Sponsors** \n\nBecome a sponsor and get your logo on our README on Github with a link to your site. [[**Become a sponsor**](https://opencollective.com/react-native-camera#sponsor)] \n\n\n ${sponsorsList.join("")}
            `,
            imageAlign: 'center',
            title: 'Open Collective',
          },
        ]}
      </Block>
    );


    return (
      <div>
        <HomeSplash siteConfig={siteConfig} language={language} />
        <div className="mainContainer">
          <Backers />
          <Support />
        </div>
      </div>
    );
  }
}

module.exports = Index;
