import React, {
  Component,
  PropTypes,
} from 'react';
import {
  ActivityIndicator,
  Platform,
  StyleSheet,
  View,
} from 'react-native';

class Viewfinder extends Component {
  constructor(props) {
    super(props);

    this.getBackgroundColor = this.getBackgroundColor.bind(this);
    this.getSizeStyles = this.getSizeStyles.bind(this);
    this.getEdgeSizeStyles = this.getEdgeSizeStyles.bind(this);
    this.renderLoadingIndicator = this.renderLoadingIndicator.bind(this);
  }

  getBackgroundColor() {
    return ({
      backgroundColor: this.props.backgroundColor,
    });
  }

  getEdgeColor() {
    return ({
      borderColor: this.props.color,
    });
  }

  getSizeStyles() {
    return ({
      height: this.props.height,
      width: this.props.width,
    });
  }

  getEdgeSizeStyles() {
    return ({
      height: this.props.borderLength,
      width: this.props.borderLength,
    });
  }

  renderLoadingIndicator() {
    if (!this.props.isLoading) {
      return null;
    }

    return (
      <ActivityIndicator
        animating={this.props.isLoading}
        color={this.props.color}
        size='large'
      />
    );
  }

  render() {
    return (
      <View style={[styles.container, this.getBackgroundColor()]}>
        <View style={[styles.viewfinder, this.getBackgroundColor(), this.getSizeStyles()]}>
          <View style={[
            this.getEdgeColor(),
            this.getEdgeSizeStyles(),
            styles.topLeftEdge,
            {
              borderLeftWidth: this.props.borderWidth,
              borderTopWidth: this.props.borderWidth,
            }
          ]} />
          <View style={[
            this.getEdgeColor(),
            this.getEdgeSizeStyles(),
            styles.topRightEdge,
            {
              borderRightWidth: this.props.borderWidth,
              borderTopWidth: this.props.borderWidth,
            }
          ]} />
          {this.renderLoadingIndicator()}
          <View style={[
            this.getEdgeColor(),
            this.getEdgeSizeStyles(),
            styles.bottomLeftEdge,
            {
              borderLeftWidth: this.props.borderWidth,
              borderBottomWidth: this.props.borderWidth,
            }
          ]} />
          <View style={[
            this.getEdgeColor(),
            this.getEdgeSizeStyles(),
            styles.bottomRightEdge,
            {
              borderRightWidth: this.props.borderWidth,
              borderBottomWidth: this.props.borderWidth,
            }
          ]} />
        </View>
      </View>
    );
  }
};

Viewfinder.propTypes = {
  backgroundColor: PropTypes.string,
  borderWidth: PropTypes.number,
  borderLength: PropTypes.number,
  color: PropTypes.string,
  height: PropTypes.number,
  isLoading: PropTypes.bool,
  width: PropTypes.number,
};

Viewfinder.defaultProps = {
  backgroundColor: 'transparent',
  borderWidth: 2,
  borderLength: 30,
  color: 'white',
  height: 200,
  isLoading: false,
  width: 200,
};

var styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    position: 'absolute',
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
  },
  viewfinder: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  topLeftEdge: {
    position: 'absolute',
    top: 0,
    left: 0,
  },
  topRightEdge: {
    position: 'absolute',
    top: 0,
    right: 0,
  },
  bottomLeftEdge: {
    position: 'absolute',
    bottom: 0,
    left: 0,
  },
  bottomRightEdge: {
    position: 'absolute',
    bottom: 0,
    right: 0,
  },
});

module.exports = Viewfinder;
