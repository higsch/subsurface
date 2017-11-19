package application.about;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class AboutDialogController {
	
	@SuppressWarnings("unused")
	private Application mainApp;
	private Stage stage;
	private WebEngine webEngine;
	
	@FXML
	private WebView webView;
	
	public void init(Application mainApp, Stage stage) {
		this.mainApp = mainApp;
		this.stage = stage;
		webEngine = webView.getEngine();
		webEngine.load(getClass().getResource("/about.html").toString());
		
		webEngine.locationProperty().addListener(new ChangeListener<String>() {
			
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if ((newValue != null) && newValue.substring(0, 4).equals("http")) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							webEngine.load(oldValue);
				        }
				    });
					
					mainApp.getHostServices().showDocument(newValue);
				}
			}
		});
	}
	
	public void handleClose() {
		stage.close();
	}
	
}
