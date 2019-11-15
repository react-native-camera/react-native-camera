import React from 'react';
import {BackHandler, Alert} from 'react-native';
import {
  Button,
  Text,
  Container,
  Icon,
  connectStyle
} from "native-base";

import {Content} from 'src/baseComponents/Content';
import MainHeader from 'src/baseComponents/MainHeader';


class Home extends React.Component {
  constructor(props){
    super(props);
    this.state = {};


    // custom exit handler
    this.props.navigation.setParams({
      'onExit': this.onExit
    });

  }

  onExit = () => {
    Alert.alert(
      'Confirm',
      'Are you sure you want to close the application?',
      [
        {
          text: 'Cancel',
          style: 'cancel',
        },
        {text: 'Yes', onPress: () => BackHandler.exitApp()},
      ],
      {cancelable: true},
    );

    return true;
  }

  render(){
    return (
      <Container>
        <Content padder>
          <Text paddingBottom>Welcome to RNCamera. Press below to test it!</Text>

          <Button
            full
            iconLeft
            onPress={() => this.props.navigation.navigate("Camera")}
            leftJustified
          >
            <Icon name='camera' type='AntDesign'/>
            <Text>Camera</Text>
          </Button>
        </Content>
      </Container>
    )
  }
}

// Different header, no back
Home.navigationOptions = ({ navigation }) => {
  return {
    header: props => <MainHeader title="Home" back={false} navigation={navigation}/>
  }
}

Home = connectStyle("Branding")(Home);

export default Home;