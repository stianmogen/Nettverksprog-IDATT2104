import React from 'react';
import axios from 'axios';

class Compiler extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      value: '',
      output: '',
      isLoading: false
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    this.setState({value: event.target.value});
  }

  handleSubmit(event) {
    event.preventDefault();

    this.setState({isLoading: true});

    axios.post('http://localhost:8080/compile', {
      code: this.state.value
    })
    .then((response) => {
      this.setState({output: response.data});
      
      this.setState({isLoading: false});

      console.log(response);
    }, (error) => {
      console.log(error);
      this.setState({output: error});

      this.setState({isLoading: false});
    });
  }

  render() {
    return (
      <div>
        <form onSubmit={this.handleSubmit}>
          <label>
            main.cpp
          </label>
          <textarea type="text" value={this.state.value} onChange={this.handleChange}/>
          <input type="submit" value="Submit" />
        </form>
        <div>
          <p>Output:</p>
          {this.state.output && <p>{this.state.output}</p>}
          {this.state.isLoading && <p>Loading...</p>}
        </div>
      </div>
    );
  }
}

export default Compiler;