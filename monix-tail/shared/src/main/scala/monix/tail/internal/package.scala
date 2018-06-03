/*
 * Copyright (c) 2014-2018 by The Monix Project Developers.
 * See the project homepage at: https://monix.io
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

package monix.tail

import cats.effect.Sync
import monix.tail.Iterant.Scope

package object internal {
  /**
    * Internal API — extension methods used in the implementation.
    */
  private[tail] implicit class ScopeExtensions[F[_], A](
    val self: Scope[F, A])
    extends AnyVal {

    def runMap[B](f: Iterant[F, A] => Iterant[F, B])(implicit F: Sync[F]) =
      self.copy(use = F.map(self.use)(f))

    def runFlatMap[B](f: Iterant[F, A] => F[Iterant[F, B]])(implicit F: Sync[F]): F[Iterant[F, B]] =
      F.pure(self.copy(use = F.flatMap(self.use)(f)))

    def runFold[B](f: Iterant[F, A] => F[B])(implicit F: Sync[F]): F[B] =
      F.bracketCase(self.open)(_ => F.flatMap(self.use)(f))((_, exitCase) => self.close(exitCase))
  }
}