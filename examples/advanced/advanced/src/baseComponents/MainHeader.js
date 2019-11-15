
import conf from 'src/conf';
import React from 'react';
import {
  BackHandler,
  StyleSheet,
} from 'react-native';

import {StyleProvider, Root, Input} from 'native-base';
import {
  Button,
  Body,
  Title,
  Left,
  Icon,
  Right,
  connectStyle
} from "native-base";

import {Header} from 'src/baseComponents/Header';

let variables = conf.theme.variables;

const styles = StyleSheet.create({
  backButton: {
    width: 48 * variables.sizeScaling,
  },
  backButtonIcon: {
    fontSize: variables.iconBackButtonSize
  }
});


export class SceneBackHandler extends React.PureComponent{

  componentDidMount(){
    this.willBlur = this.props.navigation.addListener('willBlur', this.handleBlur);
    this.willFocus = this.props.navigation.addListener('willFocus', this.handleFocus);

    // also add back handler here if it wasn't set already and we are focused
    if(this.props.navigation.isFocused() && !this.backHandler){
      this.handleFocus();
    }
  }

  componentWillUnmount(){
    this.willBlur && this.willBlur.remove();
    this.willFocus && this.willFocus.remove();

    // blur not called on component unmount
    this.backHandler && this.backHandler.remove();
  }

  handleFocus = () => {
    if(!this.backHandler){
      this.backHandler = BackHandler.addEventListener('hardwareBackPress', this.onBackPressAndroid);
    }

  }

  handleBlur = () => {
    this.backHandler && this.backHandler.remove();
    this.backHandler = null;
  }

  onBackPressAndroid = () => {
    let onExit = this.props.navigation.getParam('onExit', null);
    if(onExit){
      return onExit(this.props.navigation);
    }
  }

  render(){
    return null;
  }
}

class MainHeader extends React.PureComponent {

  handleBack = () => {
    let onExit = this.props.navigation.getParam('onExit', null);
    if(onExit){
      if(!onExit(this.props.navigation)){
        this.props.navigation.goBack(null);
      }
    }
    else{
      this.props.navigation.goBack(null);
    }
  }

  render() {
    let {transparent, back, headerStyle, title} = this.props;
    return (
      <Header transparent={transparent} style={headerStyle}>
        {back ?
          <Left>
            <Button
              transparent
              rounded
              onPress={this.handleBack}
              style={styles.backButton}
            >
              <Icon style={styles.backButtonIcon} name="arrow-back" />
            </Button>
          </Left>

        : <Left paddingRight/>}
        <Body>
          {typeof title === 'string' ? <Title padder>{title}</Title> : title}
        </Body>

        <Right>

        </Right>

        <SceneBackHandler navigation={this.props.navigation}/>
      </Header>
    );
  }
}

MainHeader = connectStyle("Branding")(MainHeader);

export default MainHeader;