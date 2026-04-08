package com.chaosthedude.explorerscompass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.network.SearchForNextPacket;
import com.chaosthedude.explorerscompass.network.SearchPacket;
import com.chaosthedude.explorerscompass.network.TeleportPacket;
import com.chaosthedude.explorerscompass.sorting.ISorting;
import com.chaosthedude.explorerscompass.sorting.NameSorting;
import com.chaosthedude.explorerscompass.util.CompassState;
import com.chaosthedude.explorerscompass.util.StructureUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class ExplorersCompassScreen extends Screen {

	private World world;
	private PlayerEntity player;
	private List<Identifier> allowedStructureIDs;
	private List<Identifier> structureIDsMatchingSearch;
	private ItemStack stack;
	private ExplorersCompassItem explorersCompass;
	private Identifier foundStructureId;
	private ButtonWidget searchButton;
	private ButtonWidget searchForNextButton;
	private ButtonWidget searchForGroupButton;
	private ButtonWidget sortByButton;
	private ButtonWidget teleportButton;
	private ButtonWidget cancelButton;
	private TransparentTextField searchTextField;
	private StructureSearchList selectionList;
	private ISorting sortingCategory;

	public ExplorersCompassScreen(World world, PlayerEntity player, ItemStack stack, ExplorersCompassItem explorersCompass, List<Identifier> allowedStructureIDs) {
		super(Text.translatable("string.explorerscompass.selectStructure"));
		this.world = world;
		this.player = player;
		this.stack = stack;
		this.explorersCompass = explorersCompass;

		this.allowedStructureIDs = new ArrayList<Identifier>(allowedStructureIDs);
		structureIDsMatchingSearch = new ArrayList<Identifier>(allowedStructureIDs);
		sortingCategory = new NameSorting();

		if (explorersCompass.getCompassState(stack) == CompassState.FOUND) {
			foundStructureId = explorersCompass.getStructureID(stack);
		}
	}

	@Override
	public boolean mouseScrolled(double par1, double par2, double par3, double par4) {
		return selectionList.mouseScrolled(par1, par2, par3, par4);
	}

	@Override
	protected void init() {
		clearChildren();
		setupButtons();
		setupTextFields();
        selectionList = new StructureSearchList(this, client, player, foundStructureId, 130, 40, width - 140, height - 50, 50);
		addDrawableChild(selectionList);
	}

	@Override
	public void tick() {
		searchForNextButton.active = teleportButton.active = selectionList.hasSelection() ? selectionList.getSelectedOrNull().getStructureId().equals(foundStructureId) : false;
		searchButton.active = searchForGroupButton.active = selectionList.hasSelection();

		// Check if the allowed structure list has synced
		if (ExplorersCompass.synced) {
			remove(selectionList);
			allowedStructureIDs = new ArrayList<Identifier>(ExplorersCompass.allowedStructureIDs);
			structureIDsMatchingSearch = new ArrayList<Identifier>(allowedStructureIDs);
			selectionList = new StructureSearchList(this, client, player, foundStructureId, 130, 40, width - 140, height - 50, 50);
			addDrawableChild(selectionList);

			teleportButton.visible = ExplorersCompass.canTeleport;
			searchForNextButton.visible = ExplorersCompass.maxNextSearches > 0;
			if (searchForNextButton.visible) {
				sortByButton.setPosition(10, 125);
			} else {
				sortByButton.setPosition(10, 100);
			}

			ExplorersCompass.synced = false;
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
		super.render(context, mouseX, mouseY, partialTicks);
		context.drawCenteredTextWithShadow(textRenderer, title, 65, 15, 0xffffff);
	}

	@Override
	public boolean keyPressed(int par1, int par2, int par3) {
		boolean ret = super.keyPressed(par1, par2, par3);
		if (searchTextField.isFocused()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}

	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		boolean ret = super.charTyped(typedChar, keyCode);
		if (searchTextField.isFocused()) {
			processSearchTerm();
			return true;
		}
		return ret;
	}

	public void searchForStructure(Identifier structureID) {
		ClientPlayNetworking.send(new SearchPacket(structureID, false));
		client.setScreen(null);
	}

	public void searchForGroup(Identifier groupId) {
		ClientPlayNetworking.send(new SearchPacket(groupId, true));
		client.setScreen(null);
	}

	public void searchForNext() {
		client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		ClientPlayNetworking.send(new SearchForNextPacket());
		client.setScreen(null);
	}

	public void teleport() {
		ClientPlayNetworking.send(new TeleportPacket());
		client.setScreen(null);
	}

	public void processSearchTerm() {
		structureIDsMatchingSearch = new ArrayList<Identifier>();
		String searchTerm = searchTextField.getText().toLowerCase();
		for (Identifier id : allowedStructureIDs) {
			if (searchTerm.startsWith("@")) {
				if (StructureUtils.getStructureSource(id).toLowerCase().contains(searchTerm.substring(1))) {
					structureIDsMatchingSearch.add(id);
				}
			} else if (StructureUtils.getStructureName(id).toLowerCase().contains(searchTerm)) {
				structureIDsMatchingSearch.add(id);
			}
		}
		selectionList.refreshList(true);
	}

	public List<Identifier> sortStructures() {
		final List<Identifier> structures = structureIDsMatchingSearch;
		Collections.sort(structures, new NameSorting());
		Collections.sort(structures, sortingCategory);
		return structures;
	}

	private void setupButtons() {
		searchButton = addDrawableChild(new TransparentButton(10, 40, 110, 20, Text.translatable("string.explorerscompass.search"), (onPress) -> {
			if (selectionList.hasSelection()) {
				searchForStructure(selectionList.getSelectedOrNull().getStructureId());
			}
		}));
		searchButton.active = false;

		searchForGroupButton = addDrawableChild(new TransparentButton(10, 65, 110, 20, Text.translatable("string.explorerscompass.searchForGroup"), (onPress) -> {
			if (selectionList.hasSelection()) {
				searchForGroup(selectionList.getSelectedOrNull().getGroupId());
			}
		}));
		searchForGroupButton.active = false;

		searchForNextButton = addDrawableChild(new TransparentButton(10, 90, 110, 20, Text.translatable("string.explorerscompass.searchForNext"), (onPress) -> {
			searchForNext();
		}));
		searchForNextButton.visible = ExplorersCompass.maxNextSearches > 0;
		searchForNextButton.active = false;

		sortByButton = addDrawableChild(new TransparentButton(10, 125, 110, 20, Text.translatable("string.explorerscompass.sortBy").append(": " + sortingCategory.getLocalizedName()), (onPress) -> {
			sortingCategory = sortingCategory.next();
			sortByButton.setMessage(Text.translatable("string.explorerscompass.sortBy").append(": " + sortingCategory.getLocalizedName()));
			selectionList.refreshList(true);
		}));
		if (!searchForNextButton.visible) {
			sortByButton.setPosition(10, 100);
		}

		teleportButton = addDrawableChild(new TransparentButton(width - 120, 10, 110, 20, Text.translatable("string.explorerscompass.teleport"), (onPress) -> {
			teleport();
		}));
		teleportButton.visible = ExplorersCompass.canTeleport;
		teleportButton.active = false;

		cancelButton = addDrawableChild(new TransparentButton(10, height - 30, 110, 20, Text.translatable("gui.cancel"), (onPress) -> {
			client.setScreen(null);
		}));
	}

	private void setupTextFields() {
		searchTextField = new TransparentTextField(textRenderer, width / 2 - 82, 10, 140, 20, Text.translatable("string.explorerscompass.search"));
		addDrawableChild(searchTextField);
	}

}
