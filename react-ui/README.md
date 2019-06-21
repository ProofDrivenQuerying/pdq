  PDQ react-ui JavaScript UI

  PQD web UI made in React

  The source code is available for free for non-commercial use.
  See the LICENCE file for details.

  I. Requirements

   * Node.js version 8.x or higher
   * React.js 16.0.0 or higher

  II. Dependencies

  External: @vx/-0.0.189, d3.js-5.9.2, redux-4.0.1, redux-thunk-2.3.0, reactstrap-8.0.0, react-icons-3.7.0

  To install dependencies and requirements:

  On MacOs/Linux:

    * brew install node
    * npm install react

  For vx modules:

    * npm i @vx/group
    * npm i @vx/hierarchy
    * npm i @vx/gradient
    * npm i @vx/shape

  For d3.js modules:

    * npm i d3-hierarchy
    * npm i d3-shape

  For redux:

    * npm install --save redux
    * npm i react-redux
    * npm install --save-dev redux-devtools
    * npm i redux-thunk

  For Bootstrap:

    * npm install --save reactstrap

  For react-icons:

    * npm install react-icons --save

  III. Installing & running the UI

  Under the top directory, type:

  	npm start

  This runs the app in the development mode.
  Open [http://localhost:3000](http://localhost:3000) to view it in the browser.
  The page will reload whenever you make local edits and save.

    npm test

  This launches the test runner in the interactive watch mode.
  See the section about [running tests](https://facebook.github.io/create-react-app/docs/running-tests) for more information.

    npm run build

  Builds the app for production to the build folder.
  It correctly bundles React in production mode and optimizes the build for the best performance.
  The build is minified and the filenames include the hashes.
  See the section about [deployment](https://facebook.github.io/create-react-app/docs/deployment) for more information.
  *There is a proxy in package.json that was used in development. Remember to remove it before building!*
