import React, { Component } from 'react';
import { Keyboard, ScrollView, Platform } from 'react-native';
import {runAfterInteractions} from './utils';


// Component similar to react-native-keyboard-aware-scroll-view
// but made for iOS only with slight modifications for nested support. TODO: Replace react-native-keyboard-aware-scroll-view at some point
// Input fields will be auto scrolled automatically. For multi line, scrollEnabled={false} must be used.
// props:
//    Component: FlatList | ScrollView
//    extraHeight
//    innerRef

const IS_IOS = Platform.OS == 'ios';


const showEvent = IS_IOS ? 'keyboardWillShow' : 'keyboardDidShow';
const hideEvent = IS_IOS ? 'keyboardWillHide' : 'keyboardDidHide';


// using native props to avoid re-renders


class KeyboardShift extends Component {
  constructor(props){
    super(props);
    this.state = {
    }
    this.scroll = null;
    this.lastScroll = null;
  }

  componentDidMount() {
    this.keyboardShowSub = Keyboard.addListener(showEvent, this.handleKeyboardShow);
    this.keyboardHideSub = Keyboard.addListener(hideEvent, this.handleKeyboardHide);
    this.mounted = true;
  }

  componentWillUnmount() {
    this.mounted = false;
    this.lastScroll = null;
    this.keyboardShowSub.remove();
    this.keyboardHideSub.remove();

    if(this.cancelHide){
      this.cancelHide();
      this.cancelHide = null;
    }

    if(this.cancelShow){
      this.cancelShow();
      this.cancelShow = null;
    }
  }

  onLayout = (event) => {
    this.layout = event.nativeEvent.layout
  }

  onScroll = (event) => {
    if(event.nativeEvent.contentOffset){
      this.lastScroll = event.nativeEvent.contentOffset.y;
    }
    this.props.onScroll && this.props.onScroll(event);
  }

  onScrollEndDrag = (event) =>{
    // if user manually scrolled, do not restore scroll
    this.scroll = null;
    this.props.onScrollEndDrag && this.props.onScrollEndDrag(event);
  }

  setRef = (r) => {
    this.ref = r;
    if(this.props.innerRef){
      this.props.innerRef(r);
    }
  }

  render() {
    const { Component, innerRef, onScroll, onScrollEndDrag, keyboardDismissMode, ...rest } = this.props;
    return (
      <Component
        ref={this.setRef}
        onLayout={this.onLayout}
        keyboardDismissMode={keyboardDismissMode}
        automaticallyAdjustContentInsets={false}
        scrollEventThrottle={16}
        onScroll={this.onScroll}
        onScrollEndDrag={this.onScrollEndDrag}
        {...rest} />
    );
  }

  scrollTo = (scroll) => {
    if(this.ref.scrollToOffset){
      this.ref.scrollToOffset({
        offset: scroll,
        animated: true
      })
    }
    else{
      this.ref.scrollTo({
        animated: true,
        y: scroll
      })
    }
  }

  handleKeyboardShow = (event) => {

    if(this.cancelHide){
      this.cancelHide();
      this.cancelHide = null;
    }

    if(this.cancelShow){
      this.cancelShow();
    }

    // need to give time to other stuff to hide if any
    // also set last scroll to 0 if it wasn't defined since we always must scroll on restore
    this.scroll = this.lastScroll || (this.lastScroll = 0);

    this.cancelShow = runAfterInteractions(()=>{
      if(this.layout && this.ref && this.mounted){

        const keyboardHeight = event.endCoordinates.height;
        //const keyboardPosition = event.endCoordinates.screenY;

        let gap = keyboardHeight + this.props.extraHeight;

        // inset is also added on timeout so it doesn't look too awkward
        this.ref.setNativeProps({contentInset: { bottom: gap }});
        this.cancelShow = null;

      }
    }, 250);

  }

  handleKeyboardHide = () => {

    if(this.cancelHide){
      this.cancelHide();
      this.cancelHide = null;
    }

    if(this.cancelShow){
      this.cancelShow();
      this.cancelShow = null;
    }

    // only fire this if we actually did something
    if(this.lastScroll != null && this.ref){

      // update inset right away to remove visible area as soon as possible
      this.ref.setNativeProps({contentInset: { bottom: 0 }});

      this.cancelHide = runAfterInteractions(()=>{
        if(this.ref && this.mounted){

          let scroll = this.scroll != null ? this.scroll : this.lastScroll;
          this.scroll = null;

          if(scroll != null && this.props.enableResetScrollToCoords){
            this.scrollTo(scroll + 0.001);
          }
          this.cancelHide = null;
        }

      }, 150);

    }


  }
}


// very similar component with a few differences
// made to work with android's android:windowSoftInputMode="adjustResize"
// other modes *might* might work, but test it.
class KeyboardShiftAndroid extends Component {
  constructor(props){
    super(props);
    this.state = {
    }
    this.scroll = null;
    this.lastScroll = null;
  }

  componentDidMount() {
    // Android events are far more limited
    this.keyboardShowSub = Keyboard.addListener(showEvent, this.handleKeyboardShow);
    this.keyboardHideSub = Keyboard.addListener(hideEvent, this.handleKeyboardHide);
    this.mounted = true;
  }

  componentWillUnmount() {
    this.mounted = false;
    this.lastScroll = null;
    this.keyboardShowSub.remove();
    this.keyboardHideSub.remove();

    if(this.cancelHide){
      this.cancelHide();
      this.cancelHide = null;
    }

    if(this.cancelShow){
      this.cancelShow();
      this.cancelShow = null;
    }
  }

  onLayout = (event) => {
    this.layout = event.nativeEvent.layout
  }

  onScroll = (event) => {
    if(event.nativeEvent.contentOffset){
      // update this with a timeout since scrolling might happen
      // before keyboard show event
      let scroll = event.nativeEvent.contentOffset.y;
      if(this.scrollTimeout){
        clearTimeout(this.scrollTimeout);
      }
      this.scrollTimeout = setTimeout(()=>{
        this.lastScroll = scroll;
      }, 260); // just a little higher than keyboard show time

    }
    this.props.onScroll && this.props.onScroll(event);
  }

  onScrollEndDrag = (event) =>{
    // if user manually scrolled, do not restore scroll
    this.scroll = null;
    this.props.onScrollEndDrag && this.props.onScrollEndDrag(event);
  }

  setRef = (r) => {
    this.ref = r;
    if(this.props.innerRef){
      this.props.innerRef(r);
    }
  }

  render() {
    const { Component, innerRef, onScroll, onScrollEndDrag, ...rest } = this.props;
    return (
      <Component
        ref={this.setRef}
        onLayout={this.onLayout}
        automaticallyAdjustContentInsets={false}
        scrollEventThrottle={16}
        onScroll={this.onScroll}
        onScrollEndDrag={this.onScrollEndDrag}
        {...rest} />
    );
  }

  scrollTo = (scroll) => {
    if(this.ref.scrollToOffset){
      this.ref.scrollToOffset({
        offset: scroll,
        animated: true
      })
    }
    else{
      this.ref.scrollTo({
        animated: true,
        y: scroll
      })
    }
  }


  // this relies on the fact that keyboard did show
  // happens before everything scrolls up due to height changes
  handleKeyboardShow = (event) => {

    if(this.cancelHide){
      this.cancelHide();
      this.cancelHide = null;
    }

    // need to give time to other stuff to hide if any
    // also set last scroll to 0 if it wasn't defined since we always must scroll on restore
    this.scroll = this.lastScroll || (this.lastScroll = 0);


  }

  handleKeyboardHide = () => {

    if(this.cancelHide){
      this.cancelHide();
      this.cancelHide = null;
    }

    // only fire this if we actually did something
    if(this.lastScroll != null && this.ref){

      this.cancelHide = runAfterInteractions(()=>{
        if(this.ref && this.mounted && this.scroll != null && this.props.enableResetScrollToCoords){
          this.scrollTo(this.scroll);
          this.cancelHide = null;
        }

      }, 150);

    }


  }
}


export default class KeyboardShiftView extends React.Component{

  render(){
    let {Component, extraHeight, innerRef, androidEnabled, ...rest} = this.props;
    return IS_IOS ?
      <KeyboardShift Component={Component} extraHeight={extraHeight} innerRef={innerRef} {...rest}/>
      :
      (androidEnabled ? <KeyboardShiftAndroid Component={Component} extraHeight={extraHeight} innerRef={innerRef} {...rest}/> : <Component ref={innerRef} {...rest}/>)
  }
}

KeyboardShiftView.defaultProps = {
  Component: ScrollView,
  extraHeight: 10,
  keyboardDismissMode: 'interactive',
  enableResetScrollToCoords: true,
  keyboardShouldPersistTaps: 'handled',
  androidEnabled: true
}