package ch.cargomedia.wms.transcoder;

import ch.cargomedia.wms.Config;
import ch.cargomedia.wms.Utils;
import ch.cargomedia.wms.module.eventhandler.ConnectionsListener;
import ch.cargomedia.wms.stream.VideostreamPublisher;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.IMediaStream;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.TimerTask;

public class Thumbnailer extends TimerTask {

  private String _input;
  private int _width;
  private int _height;

  public Thumbnailer(VideostreamPublisher videostreamPublisher, IMediaStream stream) {
    IApplicationInstance appInstance = ConnectionsListener.appInstance;
    _input = "rtmp://127.0.0.1/" + appInstance.getApplication().getName() + "/" + stream.getName();
    _width = appInstance.getProperties().getPropertyInt(Config.XMLPROPERTY_THUMBNAIL_WIDTH, 240);
    _height = (int) ((double) _width / ((videostreamPublisher.getWidth() / (double) videostreamPublisher.getHeight())));
  }

  public void run() {
    File file = _getFile();
    if (null != file) {
      try {
        _submitFile(file);
      } catch (Exception e) {
        WMSLoggerFactory.getLogger(null).error("Cannot submit thumbnail: " + e.getMessage());
      }
      file.delete();
    }
  }

  private File _getFile() {
    File file = Utils.getTempFile("png");
    String[] command = new String[]{
        "ffmpeg",
        "-threads", "1",
        "-i", _input,
        "-an",
        "-vcodec", "png",
        "-vframes", "1",
        "-f", "image2",
        "-s", String.valueOf(_width) + "x" + String.valueOf(_height),
        "-y",
        "-loglevel", "warning",
        file.getAbsolutePath(),
    };
    try {
      Utils.exec(command);
    } catch (Exception e) {
      file = null;
      WMSLoggerFactory.getLogger(null).error("Cannot capture thumbnail: " + e.getMessage());
    }
    return file;
  }

  private void _submitFile(File file) throws Exception {
    // todo
    Logger.getLogger("ch.cargomedia.wms.module.eventhandler.StreamListener").info("hello: " + file.getAbsolutePath());
  }
}