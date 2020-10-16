# [Talk] Functional Android

Talk on how to write pure functional Android apps using the brand new Arrow Fx Coroutines library.

It uses the base template for [reveal.js](https://revealjs.com/) that we use at 47academy with OBS.

<a href="https://arrow-kt.io" title="Arrow website"><img src="https://github.com/arrow-kt/arrow-site/blob/master/docs/img/home/header-image.png" width="200" alt=""></a>

## How to run the slides

For the slides, run the following commands:

```bash
./gradlew clean runAnk // validate and process slides

npm install
npm start
```
That will automatically deploy the slides in a local web server and open it in your browser.

## Animations
All animations are loaded from our CDN. This is an example:

```
https://d1bv04v5uizu9x.cloudfront.net/assets/animations/academy/refinement-types/47deg_Academy_82_Refinement_01_AType_1.webm
```

In this [link](../media/README.md), you can see the list of videos that we have in our CDN to use them in our courses.

## Style
We use the styles from our CDN. The css of our template is:

```
https://d1bv04v5uizu9x.cloudfront.net/css/reveal/47academy.css
```

## Generating a PDF

Prerequisite is to locally install `npm install decktape` in the local repo.

* Run your slides `npm start`
* Uncomment the `index.html` style where we enable `css/print/47academyPDF.css` for PDF styling. That will remove the extra margin we add to slides on the right side for presentation mode.
* To make animations render properly using their last frame, uncomment the script that sets video current time equal to its duration for all video tags.
* You will also need to uncomment the `// autoPlayMedia: false` in `Reveal.initialize` block.
* Make sure you've got all custom fonts used in the slides styles installed on your system. Fonts like Menlo-Bold, Menlo-Regular, and Changa are used.
* Keep an eye on all 2 byte emojis that might be conflictive when generating the PDF. They make decktape throw a font parsing error. Better to remove those.
* Run decktape command like this (Recommended resolution is 1440x900 once the PDF styles are enabled in previous step, since that's the same ratio we're using live):

```
`npm bin`/decktape http://localhost:8000/ ConcurrentErrorHandlingSlides.pdf --size '1440x900'
```

If you want to ignore some slides (like the usual initial OBS integration ones), you can use the `--slides` argument:

```
`npm bin`/decktape http://localhost:8000/ ConcurrentErrorHandlingSlides.pdf --size '1440x900' --slides 5-64
```
