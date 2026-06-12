package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;

public class ExperimentsScreen extends Screen {
    private static final Component TITLE = Component.translatable("selectWorld.experiments");
    private static final Component INFO = Component.translatable("selectWorld.experiments.info").withStyle(ChatFormatting.RED);
    private static final int MAIN_CONTENT_WIDTH = 310;
    private static final int SCROLL_AREA_MIN_HEIGHT = 130;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen parent;
    private final PackRepository packRepository;
    private final Consumer<PackRepository> output;
    private final Object2BooleanMap<Pack> packs = new Object2BooleanLinkedOpenHashMap<>();
    @Nullable
    private ExperimentsScreen.ScrollArea scrollArea;

    public ExperimentsScreen(Screen p_270165_, PackRepository p_270308_, Consumer<PackRepository> p_270392_) {
        super(TITLE);
        this.parent = p_270165_;
        this.packRepository = p_270308_;
        this.output = p_270392_;

        for (Pack pack : p_270308_.getAvailablePacks()) {
            if (pack.getPackSource() == PackSource.FEATURE) {
                this.packs.put(pack, p_270308_.getSelectedPacks().contains(pack));
            }
        }
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        LinearLayout linearlayout = this.layout.addToContents(LinearLayout.vertical());
        linearlayout.addChild(new MultiLineTextWidget(INFO, this.font).setMaxWidth(310), p_293611_ -> p_293611_.paddingBottom(15));
        SwitchGrid.Builder switchgrid$builder = SwitchGrid.builder(299).withInfoUnderneath(2, true).withRowSpacing(4);
        this.packs
            .forEach(
                (p_270880_, p_270874_) -> switchgrid$builder.addSwitch(
                            getHumanReadableTitle(p_270880_),
                            () -> this.packs.getBoolean(p_270880_),
                            p_270491_ -> this.packs.put(p_270880_, p_270491_.booleanValue())
                        )
                        .withInfo(p_270880_.getDescription())
            );
        Layout layout = switchgrid$builder.build().layout();
        this.scrollArea = new ExperimentsScreen.ScrollArea(layout, 310, 130);
        linearlayout.addChild(this.scrollArea);
        LinearLayout linearlayout1 = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        linearlayout1.addChild(Button.builder(CommonComponents.GUI_DONE, p_270336_ -> this.onDone()).build());
        linearlayout1.addChild(Button.builder(CommonComponents.GUI_CANCEL, p_274702_ -> this.onClose()).build());
        this.layout.visitWidgets(p_321381_ -> {
            AbstractWidget abstractwidget = this.addRenderableWidget(p_321381_);
        });
        this.repositionElements();
    }

    private static Component getHumanReadableTitle(Pack p_270861_) {
        String s = "dataPack." + p_270861_.getId() + ".name";
        return (Component)(I18n.exists(s) ? Component.translatable(s) : p_270861_.getTitle());
    }

    @Override
    protected void repositionElements() {
        this.scrollArea.setHeight(130);
        this.layout.arrangeElements();
        int i = this.height - this.layout.getFooterHeight() - this.scrollArea.getRectangle().bottom();
        this.scrollArea.setHeight(this.scrollArea.getHeight() + i);
        this.scrollArea.refreshScrollAmount();
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), INFO);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void onDone() {
        List<Pack> list = new ArrayList<>(this.packRepository.getSelectedPacks());
        List<Pack> list1 = new ArrayList<>();
        this.packs.forEach((p_270540_, p_270780_) -> {
            list.remove(p_270540_);
            if (p_270780_) {
                list1.add(p_270540_);
            }
        });
        list.addAll(Lists.reverse(list1));
        this.packRepository.setSelected(list.stream().map(Pack::getId).toList());
        this.output.accept(this.packRepository);
    }

    public class ScrollArea extends AbstractContainerWidget {
        private final List<AbstractWidget> children = new ArrayList<>();
        private final Layout layout;

        public ScrollArea(Layout p_387711_, int p_388705_, int p_387334_) {
            super(0, 0, p_388705_, p_387334_, CommonComponents.EMPTY);
            this.layout = p_387711_;
            p_387711_.visitWidgets(this::addWidget);
        }

        public void addWidget(AbstractWidget p_388235_) {
            this.children.add(p_388235_);
        }

        @Override
        protected int contentHeight() {
            return this.layout.getHeight();
        }

        @Override
        protected double scrollRate() {
            return 10.0;
        }

        @Override
        protected void renderWidget(GuiGraphics p_387127_, int p_388038_, int p_386932_, float p_388026_) {
            p_387127_.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);
            p_387127_.pose().pushPose();
            p_387127_.pose().translate(0.0, -this.scrollAmount(), 0.0);

            for (AbstractWidget abstractwidget : this.children) {
                abstractwidget.render(p_387127_, p_388038_, p_386932_, p_388026_);
            }

            p_387127_.pose().popPose();
            p_387127_.disableScissor();
            this.renderScrollbar(p_387127_);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput p_388854_) {
        }

        @Override
        public ScreenRectangle getBorderForArrowNavigation(ScreenDirection p_388582_) {
            return new ScreenRectangle(this.getX(), this.getY(), this.width, this.contentHeight());
        }

        @Override
        public void setFocused(@Nullable GuiEventListener p_387564_) {
            super.setFocused(p_387564_);
            if (p_387564_ != null) {
                ScreenRectangle screenrectangle = this.getRectangle();
                ScreenRectangle screenrectangle1 = p_387564_.getRectangle();
                int i = (int)((double)screenrectangle1.top() - this.scrollAmount() - (double)screenrectangle.top());
                int j = (int)((double)screenrectangle1.bottom() - this.scrollAmount() - (double)screenrectangle.bottom());
                if (i < 0) {
                    this.setScrollAmount(this.scrollAmount() + (double)i - 14.0);
                } else if (j > 0) {
                    this.setScrollAmount(this.scrollAmount() + (double)j + 14.0);
                }
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public void setX(int p_388848_) {
            super.setX(p_388848_);
            this.layout.setX(p_388848_);
            this.layout.arrangeElements();
        }

        @Override
        public void setY(int p_387427_) {
            super.setY(p_387427_);
            this.layout.setY(p_387427_);
            this.layout.arrangeElements();
        }

        @Override
        public Collection<? extends NarratableEntry> getNarratables() {
            return this.children;
        }
    }
}
