import React from 'react';
import {InteractionManager} from 'react-native';
import {Spinner, connectStyle} from "native-base";


// helper that calls interaction manager run after interactions
// but with a tiny timeout to also give time to other code to run
// some RN change broke runAfterInteractions in a way that it no longer has a huge delay as it used to
// so we add one here
// returns a cancellable object
export function runAfterInteractions(fun, to=25){
  // setTimeout(() => {
  //   InteractionManager.runAfterInteractions(fun)
  // }, to);

  let prom = null;
  let timeout = setTimeout(() => {
    prom = InteractionManager.runAfterInteractions(fun);
  }, to);

  return () => {
    clearTimeout(timeout);
    if(prom){
      prom.cancel();
      prom = null;
    }
  }
}



class StyledSpinner extends React.PureComponent{
  render(){
    return <Spinner color={this.props.style.brandPrimary}/>
  }
}

StyledSpinner = connectStyle("Branding")(StyledSpinner);


export {StyledSpinner};