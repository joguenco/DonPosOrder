package dev.joguenco.ping.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/donpos/order/v1")
public class PingController {

  @Get("/ping")
  HttpResponse<Ping> ping() {
    return HttpResponse.ok(new Ping("pong"));
  }
}
