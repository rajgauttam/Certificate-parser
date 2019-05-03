package com.cisco.usm.app.certificates.client.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.cisco.usm.app.certificates.client.presenter.ShowCertificateUIHandler;
import com.cisco.usm.client.core.policy.view.dialog.OkCancelDialogView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.data.shared.TreeStore.TreeNode;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.tree.Tree;

public class ShowCertificateDialogViewImpl extends OkCancelDialogView implements ShowCertificateDialogView {

    private static int autoId;
	ShowCertificateUIHandler shoeCertificateUiHandler;
    private static final String DIALOG_HEIGHT = "450";
    private static final String DIALOG_WIDTH = "600";

    interface Binder extends UiBinder<VerticalLayoutContainer, ShowCertificateDialogViewImpl> {
    }

    @UiField
    VerticalLayoutContainer container;
    
    @UiField(provided = true)
    TreeStore<BaseDto> store = new TreeStore<BaseDto>(new KeyProvider());

    @UiField
    Tree<BaseDto, String> tree;

    private static final Binder uiBinder = GWT.create(Binder.class);

    @Inject
    public ShowCertificateDialogViewImpl(EventBus eventBus) {
        super(eventBus);
        container = (VerticalLayoutContainer)constructUi();//uiBinder.createAndBindUi(this);
        setDialog();

    }

    @Override
    public void setUiHandlers(ShowCertificateUIHandler uiHandlers) {
        this.shoeCertificateUiHandler = uiHandlers;
    }

    private void setDialog() {
        dialog.setHeadingText("Certificate");
        dialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        dialog.setOnEsc(true);
        dialog.setShadow(false);
        dialog.setModal(true);
        dialog.setBorders(true);
        dialog.setAnimCollapse(false);
        dialog.setCollapsible(true);
        dialog.setHideOnButtonClick(false);
        dialog.setResizable(false);
        dialog.add(container);

    }

    @UiFactory
    public ValueProvider<BaseDto, String> createValueProvider() {
      return new ValueProvider<BaseDto, String>() {

        @Override
        public String getValue(BaseDto object) {
          return object.getName();
        }

        @Override
        public void setValue(BaseDto object, String value) {
        }

        @Override
        public String getPath() {
          return "name";
        }
      };
    }

    @UiHandler("expandAll")
    public void expandAll(SelectEvent event) {
      tree.expandAll();
    }

    @UiHandler("collapseAll")
    public void collapseAll(SelectEvent event) {
      tree.collapseAll();
    }
    
   

    private Widget constructUi() {
      FolderDto root = getMusicRootFolder();
      for (BaseDto base : root.getChildren()) {
        store.add(base);
        if (base instanceof FolderDto) {
          processFolder(store, (FolderDto) base);
        }
      }

      Widget widget = uiBinder.createAndBindUi(this);
      widget.addStyleName("margin-10");
      tree.getStyle().setNodeOpenIcon(null);
      tree.getStyle().setNodeCloseIcon(null);
      tree.getStyle().setJointCloseIcon(null);
      tree.getStyle().setJointCloseIcon(null);
     // tree.getStyle().setLeafIcon(ExampleImages.INSTANCE.music());
      return widget;
    }

    private void processFolder(TreeStore<BaseDto> store, FolderDto folder) {
      for (BaseDto child : folder.getChildren()) {
        store.add(folder, child);
        if (child instanceof FolderDto) {
          processFolder(store, (FolderDto) child);
        }
      }
    }   

    public  FolderDto getMusicRootFolder() {
        FolderDto root = makeFolder("Root");

        FolderDto author = makeFolder("Beethoven");
        List<BaseDto> children = new ArrayList<BaseDto>();
        children.add(author);
        root.setChildren(children);

        FolderDto genre = makeFolder("Quartets");
        author.addChild(genre);

        genre.addChild(makeMusic("Six String Quartets", author, genre));
        genre.addChild(makeMusic("Three String Quartets", author, genre));
        genre.addChild(makeMusic("Grosse Fugue for String Quartets", author, genre));

        genre = makeFolder("Sonatas");
        author.addChild(genre);

        genre.addChild(makeMusic("Sonata in A Minor", author, genre));
        genre.addChild(makeMusic("Sonata in F Major", author, genre));

        genre = makeFolder("Concertos");
        author.addChild(genre);

        genre.addChild(makeMusic("No. 1 - C", author, genre));
        genre.addChild(makeMusic("No. 2 - B-Flat Major", author, genre));
        genre.addChild(makeMusic("No. 3 - C Minor", author, genre));
        genre.addChild(makeMusic("No. 4 - G Major", author, genre));
        genre.addChild(makeMusic("No. 5 - E-Flat Major", author, genre));

        genre = makeFolder("Symphonies");
        author.addChild(genre);

        genre.addChild(makeMusic("No. 1 - C Major", author, genre));
        genre.addChild(makeMusic("No. 2 - D Major", author, genre));
        genre.addChild(makeMusic("No. 3 - E-Flat Major", author, genre));
        genre.addChild(makeMusic("No. 4 - B-Flat Major", author, genre));
        genre.addChild(makeMusic("No. 5 - C Minor", author, genre));
        genre.addChild(makeMusic("No. 6 - F Major", author, genre));
        genre.addChild(makeMusic("No. 7 - A Major", author, genre));
        genre.addChild(makeMusic("No. 8 - F Major", author, genre));
        genre.addChild(makeMusic("No. 9 - D Minor", author, genre));

        author = makeFolder("Brahms");
        root.addChild(author);

        genre = makeFolder("Concertos");
        author.addChild(genre);

        genre.addChild(makeMusic("Violin Concerto", author, genre));
        genre.addChild(makeMusic("Double Concerto - A Minor", author, genre));
        genre.addChild(makeMusic("Piano Concerto No. 1 - D Minor", author, genre));
        genre.addChild(makeMusic("Piano Concerto No. 2 - B-Flat Major", author, genre));

        genre = makeFolder("Quartets");
        author.addChild(genre);

        genre.addChild(makeMusic("Piano Quartet No. 1 - G Minor", author, genre));
        genre.addChild(makeMusic("Piano Quartet No. 2 - A Major", author, genre));
        genre.addChild(makeMusic("Piano Quartet No. 3 - C Minor", author, genre));
        genre.addChild(makeMusic("String Quartet No. 3 - B-Flat Minor", author, genre));

        genre = makeFolder("Sonatas");
        author.addChild(genre);

        genre.addChild(makeMusic("Two Sonatas for Clarinet - F Minor", author, genre));
        genre.addChild(makeMusic("Two Sonatas for Clarinet - E-Flat Major", author, genre));

        genre = makeFolder("Symphonies");
        author.addChild(genre);

        genre.addChild(makeMusic("No. 1 - C Minor", author, genre));
        genre.addChild(makeMusic("No. 2 - D Minor", author, genre));
        genre.addChild(makeMusic("No. 3 - F Major", author, genre));
        genre.addChild(makeMusic("No. 4 - E Minor", author, genre));

        author = makeFolder("Mozart");
        root.addChild(author);

        genre = makeFolder("Concertos");
        author.addChild(genre);

        genre.addChild(makeMusic("Piano Concerto No. 12", author, genre));
        genre.addChild(makeMusic("Piano Concerto No. 17", author, genre));
        genre.addChild(makeMusic("Clarinet Concerto", author, genre));
        genre.addChild(makeMusic("Violin Concerto No. 5", author, genre));
        genre.addChild(makeMusic("Violin Concerto No. 4", author, genre));

        return root;
      }
    
    private  FolderDto makeFolder(String name) {
        FolderDto theReturn = new FolderDto(++autoId, name);
        theReturn.setChildren((List<BaseDto>) new ArrayList<BaseDto>());
        return theReturn;
      }

      private  MusicDto makeMusic(String name, FolderDto author, FolderDto genre) {
        return makeMusic(name, author.getName(), genre.getName());
      }

      private  MusicDto makeMusic(String name, String author, String genre) {
        return new MusicDto(++autoId, name, genre, author);
      }

public  class BaseDto implements Serializable, TreeNode<BaseDto> {

  private Integer id;
  private String name;
  
  protected BaseDto() {
    
  }
  
  public BaseDto(Integer id, String name) {
    this.id = id;
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public BaseDto getData() {
    return this;
  }

  @Override
  public List<? extends TreeNode<BaseDto>> getChildren() {
    return null;
  }
  
  @Override
  public String toString() {
    return name != null ? name : super.toString();
  }
  }

@SuppressWarnings("serial")
public class MusicDto extends BaseDto {

  private String genre;
  private String author;

  protected MusicDto() {

  }

  public MusicDto(Integer id, String name, String genre, String author) {
    super(id, name);
    this.genre = genre;
    this.author = author;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
}

 class KeyProvider implements ModelKeyProvider<BaseDto> {
	    @Override
	    public String getKey(BaseDto item) {
	      return (item instanceof FolderDto ? "f-" : "m-") + item.getId().toString();
	    }
	  }

  @SuppressWarnings("serial")
  public  class FolderDto extends BaseDto {

    private List<BaseDto> children;

    protected FolderDto() {

    }

    public FolderDto(Integer id, String name) {
      super(id, name);
    }

    public List<BaseDto> getChildren() {
      return children;
    }

    public void setChildren(List<BaseDto> children) {
      this.children = children;
    }

    public void addChild(BaseDto child) {
      getChildren().add(child);
    }
  }

}
