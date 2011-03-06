/*
 * Copyright (c) 2009-2011 Ansgar Konermann
 *
 * This file is part of the Maven 3 Drools Plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.model.location;

public class Distance {

  private City from;
  private City to;
  private int distance;

  public Distance(City from, City to, int distance) {
    this.from = from;
    this.to = to;
    this.distance = distance;
  }

  public City getFrom() {
    return from;
  }

  public void setFrom(City from) {
    this.from = from;
  }

  public City getTo() {
    return to;
  }

  public void setTo(City to) {
    this.to = to;
  }

  public int getDistance() {
    return distance;
  }

  public void setDistance(int distance) {
    this.distance = distance;
  }
}
