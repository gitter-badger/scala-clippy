package com.softwaremill.clippy

import org.scalatest.{FlatSpec, Matchers}

class CompilationErrorParserTest extends FlatSpec with Matchers {
  it should "parse akka's route error message" in {
    val e =
      """type mismatch;
        | found   : akka.http.scaladsl.server.StandardRoute
        | required: akka.stream.scaladsl.Flow[akka.http.scaladsl.model.HttpRequest,akka.http.scaladsl.model.HttpResponse,Any]""".stripMargin

    CompilationErrorParser.parse(e) should be (Some(TypeMismatchError(
      Exact("akka.http.scaladsl.server.StandardRoute"),
      None,
      Exact("akka.stream.scaladsl.Flow[akka.http.scaladsl.model.HttpRequest,akka.http.scaladsl.model.HttpResponse,Any]"),
      None
    )))
  }

  it should "parse an error message with [error] prefix" in {
    val e =
      """[error] /Users/adamw/projects/clippy/tests/src/main/scala/com/softwaremill/clippy/Working.scala:16: type mismatch;
        |[error]  found   : akka.http.scaladsl.server.StandardRoute
        |[error]  required: akka.stream.scaladsl.Flow[akka.http.scaladsl.model.HttpRequest,akka.http.scaladsl.model.HttpResponse,Any]""".stripMargin

    CompilationErrorParser.parse(e) should be (Some(TypeMismatchError(
      Exact("akka.http.scaladsl.server.StandardRoute"),
      None,
      Exact("akka.stream.scaladsl.Flow[akka.http.scaladsl.model.HttpRequest,akka.http.scaladsl.model.HttpResponse,Any]"),
      None
    )))
  }

  it should "parse a type mismatch error with a single expands to section" in {
    val e =
      """type mismatch;
        |found   : japgolly.scalajs.react.CompState.ReadCallbackWriteCallbackOps[com.softwaremill.clippy.Contribute.Step2.State]#This[com.softwaremill.clippy.FormField]
        |required: japgolly.scalajs.react.CompState.AccessRD[?]
        |   (which expands to)  japgolly.scalajs.react.CompState.ReadDirectWriteCallbackOps[?]""".stripMargin

    CompilationErrorParser.parse(e) should be (Some(TypeMismatchError(
      Exact("japgolly.scalajs.react.CompState.ReadCallbackWriteCallbackOps[com.softwaremill.clippy.Contribute.Step2.State]#This[com.softwaremill.clippy.FormField]"),
      None,
      Exact("japgolly.scalajs.react.CompState.AccessRD[?]"),
      Some(Exact("japgolly.scalajs.react.CompState.ReadDirectWriteCallbackOps[?]"))
    )))
  }

  it should "parse a type mismatch error with two expands to sections" in {
    val e =
      """type mismatch;
        |found   : japgolly.scalajs.react.CompState.ReadCallbackWriteCallbackOps[com.softwaremill.clippy.Contribute.Step2.State]#This[com.softwaremill.clippy.FormField]
        |   (which expands to)  japgolly.scalajs.react.CompState.ReadCallbackWriteCallbackOps[com.softwaremill.clippy.FormField]
        |required: japgolly.scalajs.react.CompState.AccessRD[?]
        |   (which expands to)  japgolly.scalajs.react.CompState.ReadDirectWriteCallbackOps[?]""".stripMargin

    CompilationErrorParser.parse(e) should be (Some(TypeMismatchError(
      Exact("japgolly.scalajs.react.CompState.ReadCallbackWriteCallbackOps[com.softwaremill.clippy.Contribute.Step2.State]#This[com.softwaremill.clippy.FormField]"),
      Some(Exact("japgolly.scalajs.react.CompState.ReadCallbackWriteCallbackOps[com.softwaremill.clippy.FormField]")),
      Exact("japgolly.scalajs.react.CompState.AccessRD[?]"),
      Some(Exact("japgolly.scalajs.react.CompState.ReadDirectWriteCallbackOps[?]"))
    )))
  }

  it should "parse macwire's wire not found error message" in {
    val e = "not found: value wire"

    CompilationErrorParser.parse(e) should be (Some(NotFoundError(Exact("value wire"))))
  }
}
