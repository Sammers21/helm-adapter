/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 artipie.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.artipie.helm;

import com.artipie.asto.fs.FileStorage;
import com.artipie.http.rs.RsStatus;
import com.artipie.vertx.VertxSliceServer;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.WebClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Push helm chart and ensure if index.yaml is generated properly.
 *
 * @since 0.2
 */
public class SubmitChartITCase {

    @Test
    public void indexYamlIsCorrect(@TempDir final Path temp) throws IOException {
        final Vertx vertx = Vertx.vertx();
        final FileStorage fls = new FileStorage(temp, vertx.fileSystem());
        final VertxSliceServer server = new VertxSliceServer(
            vertx,
            new HelmSlice(fls)
        );
        final int port = server.start();
        final WebClient web = WebClient.create(vertx);
        final int code = web.post(port, "localhost", "/")
            .rxSendBuffer(
                Buffer.buffer(
                    Files.readAllBytes(
                        Paths.get("./src/test/resources/tomcat-0.4.1.tgz")
                    )
                )
            )
            .blockingGet()
            .statusCode();
        MatcherAssert.assertThat(
            code,
            new IsEqual<>(Integer.parseInt(RsStatus.OK.code()))
        );
        web.close();
        server.close();
        vertx.close();
    }

}
